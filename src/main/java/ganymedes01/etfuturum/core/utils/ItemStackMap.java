package ganymedes01.etfuturum.core.utils;
/*
 * Copyright (c) 2022, GTNH Team, glee8e, Code Chicken, This file is originally part of NotEnoughtItem by Code Chicken.
 * It is adapted to implement the standard Map interface by glee8e This program is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 2.1 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

import codechicken.nei.ItemStackSet;
import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

/**
 * A map class for ItemStack keys with wildcard damage/NBT. Optimised for lookup.
 * <p>
 * This map does NOT support null values or null keys! null will be silently ignored!
 * <p>
 * Originally created by CodeChicken for NotEnoughItems. Adapted to {@code Map<ItemStack, T>} interface by glee8e
 * <p>
 * Edited later to add the option to ignore NBT tags to match vanilla furnace recipe map behavior.
 *
 * @author CodeChiken
 * @author glee8e, mitchej123
 */
@SuppressWarnings("unused")
public class ItemStackMap<T> extends AbstractMap<ItemStack, T> {

	public static final NBTTagCompound WILDCARD_TAG;

	static {
		WILDCARD_TAG = new NBTTagCompound();
		WILDCARD_TAG.setBoolean("*", true);
	}

	private final HashMap<Item, DetailMap> itemMap = new HashMap<>();
	private int size;

	private final boolean NBTSensitive;

	static ItemStack newItemStack(Item item, int size, int damage, NBTTagCompound tag) {
		ItemStackSet set = null;
		ItemStack stack = new ItemStack(item, size, damage);
		stack.stackTagCompound = tag;
		return stack;
	}

	static int actualDamage(ItemStack stack) {
		return Items.diamond.getDamage(stack);
	}

	static ItemStack wildcard(Item item) {
		return wildcard(item, false);
	}

	static ItemStack wildcard(Item item, boolean nbtsensitive) {
		return newItemStack(item, 1, WILDCARD_VALUE, nbtsensitive ? WILDCARD_TAG : null);
	}

	static boolean isWildcard(int damage) {
		return damage == WILDCARD_VALUE;
	}

	static boolean isWildcard(NBTTagCompound tag) {
		return tag != null && tag.getBoolean("*");
	}

	public ItemStackMap() {
		this.NBTSensitive = false;
	}

	public ItemStackMap(boolean NBTSensitive) {
		this.NBTSensitive = NBTSensitive;
	}

	@Override
	public T get(Object key) {
		if (!(key instanceof ItemStack stack)) return null;
		if (stack.getItem() == null) return null;
		DetailMap map = itemMap.get(stack.getItem());
		return map == null ? null : map.get(stack);
	}

	@Override
	public T put(ItemStack key, T value) {
		if (key == null || key.getItem() == null || value == null) return null;
		return itemMap.computeIfAbsent(key.getItem(), k -> new DetailMap(this.NBTSensitive)).put(key, value);
	}

	@Override
	public T remove(Object key) {
		if (!(key instanceof ItemStack stack)) return null;
		if (stack.getItem() == null) return null;
		DetailMap map = itemMap.get(stack.getItem());
		return map == null ? null : map.remove(stack);
	}

	@Override
	public boolean containsKey(Object key) {
		if (!(key instanceof ItemStack stack)) return false;
		if (stack.getItem() == null) return false;
		DetailMap map = itemMap.get(stack.getItem());
		return map != null && map.get(stack) != null;
	}

	@Override
	public T merge(ItemStack key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
		if (key == null || key.getItem() == null || value == null || remappingFunction == null) return null;
		DetailMap map = itemMap.get(key.getItem());
		return itemMap.computeIfAbsent(key.getItem(), k -> new DetailMap(this.NBTSensitive))
				.merge(key, value, remappingFunction);
	}

	@Override
	public T computeIfAbsent(ItemStack key, Function<? super ItemStack, ? extends T> mappingFunction) {
		if (key == null || key.getItem() == null || mappingFunction == null) return null;
		DetailMap map = itemMap.get(key.getItem());
		return itemMap.computeIfAbsent(key.getItem(), k -> new DetailMap(this.NBTSensitive))
				.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public void clear() {
		itemMap.clear();
		size = 0;
	}

	@Override
	public Set<Map.Entry<ItemStack, T>> entrySet() {
		return new SetView();
	}

	private static class StackMetaKey {

		public final int damage;
		public final NBTTagCompound tag;

		public StackMetaKey(int damage, NBTTagCompound tag) {
			this.damage = damage;
			this.tag = tag;
		}

		public StackMetaKey(ItemStack key) {
			this(actualDamage(key), key.stackTagCompound);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(damage, tag);
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof StackMetaKey t)) return false;
			return damage == t.damage && Objects.equal(tag, t.tag);
		}
	}

	private static class DetailIter<T> implements Iterator<Map.Entry<ItemStack, T>> {

		private final Item owner;
		private final ItemStackMap<T>.DetailMap backing;

		@Nullable
		private final Iterator<Map.Entry<Integer, T>> damageIter;

		@Nullable
		private final Iterator<Map.Entry<NBTTagCompound, T>> tagIter;

		@Nullable
		private final Iterator<Map.Entry<StackMetaKey, T>> metaIter;

		private DetailIterState state = DetailIterState.NOT_STARTED;
		private boolean removed = false;

		private DetailIter(Map.Entry<Item, ItemStackMap<T>.DetailMap> input) {
			this.owner = input.getKey();
			this.backing = input.getValue();
			damageIter = backing.damageMap != null ? backing.damageMap.entrySet().iterator() : null;
			tagIter = backing.tagMap != null ? backing.tagMap.entrySet().iterator() : null;
			metaIter = backing.metaMap != null ? backing.metaMap.entrySet().iterator() : null;
		}

		private DetailIterState nextState() {
			switch (state) {
				case NOT_STARTED:
					if (backing.hasWildcard) return DetailIterState.WILDCARD;
				case WILDCARD:
				case DAMAGE:
					if (damageIter != null && damageIter.hasNext()) return DetailIterState.DAMAGE;
				case TAG:
					if (tagIter != null && tagIter.hasNext()) return DetailIterState.TAG;
				case META:
					if (metaIter != null && metaIter.hasNext()) return DetailIterState.META;
				case DONE:
					return DetailIterState.DONE;
				default:
					throw new IllegalStateException("Unexpected value: " + state);
			}
		}

		@Override
		public void remove() {
			if (removed) throw new IllegalStateException("remove() called twice");
			state.remove(this);
			removed = true;
		}

		@Override
		public boolean hasNext() {
			return nextState() != DetailIterState.DONE;
		}

		@Override
		public Map.Entry<ItemStack, T> next() {
			DetailIterState nextState = nextState();
			if (nextState == DetailIterState.DONE) throw new NoSuchElementException();
			state = nextState;
			removed = false;
			return nextState.get(this);
		}

		private enum DetailIterState {

			NOT_STARTED {
				@Override
				<T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
					throw new AssertionError("Should not call get on NOT_STARTED");
				}

				@Override
				<T> void remove(DetailIter<T> iter) {
					throw new IllegalStateException("next() never called");
				}
			},
			WILDCARD {
				@Override
				<T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
					return new Map.Entry<ItemStack, T>() {

						private final ItemStack key = wildcard(iter.owner, iter.backing.NBTSensitive);

						@Override
						public ItemStack getKey() {
							return key;
						}

						@Override
						public T getValue() {
							return iter.backing.wildcard;
						}

						@Override
						public T setValue(T value) {
							return iter.backing.wildcard = value;
						}
					};
				}

				@Override
				<T> void remove(DetailIter<T> iter) {
					assert iter.backing.hasWildcard;
					iter.backing.wildcard = null;
					iter.backing.hasWildcard = false;
				}
			},
			DAMAGE {
				@Override
				<T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
					assert iter.damageIter != null;
					Map.Entry<Integer, T> entry = iter.damageIter.next();
					return new ItemStackEntry<>(
							newItemStack(
									iter.owner,
									1,
									entry.getKey(),
									iter.backing.NBTSensitive ? WILDCARD_TAG : null),
							entry);
				}

				@Override
				<T> void remove(DetailIter<T> iter) {
					assert iter.damageIter != null;
					iter.damageIter.remove();
				}
			},
			TAG {
				@Override
				<T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
					assert iter.tagIter != null;
					Map.Entry<NBTTagCompound, T> entry = iter.tagIter.next();
					return new ItemStackEntry<>(newItemStack(iter.owner, 1, WILDCARD_VALUE, entry.getKey()), entry);
				}

				@Override
				<T> void remove(DetailIter<T> iter) {
					assert iter.tagIter != null;
					iter.tagIter.remove();
				}
			},
			META {
				@Override
				<T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
					assert iter.metaIter != null;
					Map.Entry<StackMetaKey, T> entry = iter.metaIter.next();
					return new ItemStackEntry<>(
							newItemStack(iter.owner, 1, entry.getKey().damage, entry.getKey().tag),
							entry);
				}

				@Override
				<T> void remove(DetailIter<T> iter) {
					assert iter.metaIter != null;
					iter.metaIter.remove();
				}
			},
			DONE {
				@Override
				<T> Map.Entry<ItemStack, T> get(DetailIter<T> iter) {
					throw new AssertionError("Should not call get on DONE");
				}

				@Override
				<T> void remove(DetailIter<T> iter) {
					throw new AssertionError("Should not call remove on DONE");
				}
			};

			abstract <T> Map.Entry<ItemStack, T> get(DetailIter<T> iter);

			abstract <T> void remove(DetailIter<T> iter);

			private static class ItemStackEntry<T> implements Map.Entry<ItemStack, T> {

				private final ItemStack key;
				private final Map.Entry<?, T> entry;

				public ItemStackEntry(ItemStack key, Map.Entry<?, T> entry) {
					this.key = key;
					this.entry = entry;
				}

				@Override
				public ItemStack getKey() {
					return key;
				}

				@Override
				public T getValue() {
					return entry.getValue();
				}

				@Override
				public T setValue(T value) {
					return entry.setValue(value);
				}
			}
		}
	}

	private class DetailMap {

		private boolean hasWildcard;
		private T wildcard;
		private HashMap<Integer, T> damageMap;
		private HashMap<NBTTagCompound, T> tagMap;
		private HashMap<StackMetaKey, T> metaMap;
		private int size;

		private final boolean NBTSensitive;

		public DetailMap(boolean NBTSensitive) {
			this.NBTSensitive = NBTSensitive;
		}

		private KeyType getKeyType(int damage, NBTTagCompound tag) {
			KeyType i = isWildcard(damage) ? KeyType.WildcardMeta : KeyType.NotWildcard;
			if (!NBTSensitive || isWildcard(tag)) i = i.withWildcardTag();
			return i;
		}

		public T get(ItemStack key) {
			if (wildcard != null) return wildcard;

			if (damageMap != null) {
				final T ret = damageMap.get(actualDamage(key));
				if (ret != null) return ret;
			}
			if (tagMap != null) {
				final T ret = tagMap.get(key.stackTagCompound);
				if (ret != null) return ret;
			}
			if (metaMap != null) return metaMap.get(new StackMetaKey(key));

			return null;
		}

		public T put(ItemStack key, T value) {
			try {
				switch (getKeyType(actualDamage(key), key.stackTagCompound)) {
					case NotWildcard:
						if (metaMap == null) metaMap = new HashMap<>();
						return metaMap.put(new StackMetaKey(key), value);
					case WildcardMeta:
						if (tagMap == null) tagMap = new HashMap<>();
						return tagMap.put(key.stackTagCompound, value);
					case WildcardTag:
						if (damageMap == null) damageMap = new HashMap<>();
						return damageMap.put(actualDamage(key), value);
					case WildcardAll:
						T ret = wildcard;
						wildcard = value;
						hasWildcard = true;
						return ret;
				}
			} finally {
				updateSize();
			}
			return null;
		}

		public T remove(ItemStack key) {
			try {
				switch (getKeyType(actualDamage(key), key.stackTagCompound)) {
					case NotWildcard:
						return metaMap != null ? metaMap.remove(new StackMetaKey(key)) : null;
					case WildcardMeta:
						return tagMap != null ? tagMap.remove(key.stackTagCompound) : null;
					case WildcardTag:
						return damageMap != null ? damageMap.remove(actualDamage(key)) : null;
					case WildcardAll:
						T ret = wildcard;
						wildcard = null;
						hasWildcard = false;
						return ret;
				}
			} finally {
				updateSize();
			}
			return null;
		}

		private void updateSize() {
			int newSize = (hasWildcard ? 1 : 0) + (metaMap != null ? metaMap.size() : 0)
					+ (tagMap != null ? tagMap.size() : 0)
					+ (damageMap != null ? damageMap.size() : 0);

			if (newSize != size) {
				ItemStackMap.this.size += newSize - size;
				size = newSize;
			}
		}

		public T merge(ItemStack key, T value, BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
			try {
				switch (getKeyType(actualDamage(key), key.stackTagCompound)) {
					case NotWildcard:
						if (metaMap == null) metaMap = new HashMap<>();
						return metaMap.merge(new StackMetaKey(key), value, remappingFunction);
					case WildcardMeta:
						if (tagMap == null) tagMap = new HashMap<>();
						return tagMap.merge(key.stackTagCompound, value, remappingFunction);
					case WildcardTag:
						if (damageMap == null) damageMap = new HashMap<>();
						return damageMap.merge(actualDamage(key), value, remappingFunction);
					case WildcardAll:
						T newValue = wildcard == null ? value : remappingFunction.apply(wildcard, value);
						wildcard = newValue;
						hasWildcard = newValue != null;
						return newValue;
				}
			} finally {
				updateSize();
			}
			return null;
		}

		public T computeIfAbsent(ItemStack key, Function<? super ItemStack, ? extends T> mappingFunction) {
			try {
				switch (getKeyType(actualDamage(key), key.stackTagCompound)) {
					case NotWildcard:
						if (metaMap == null) metaMap = new HashMap<>();
						return metaMap.computeIfAbsent(new StackMetaKey(key), x -> mappingFunction.apply(key));
					case WildcardMeta:
						if (tagMap == null) tagMap = new HashMap<>();
						return tagMap.computeIfAbsent(key.stackTagCompound, x -> mappingFunction.apply(key));
					case WildcardTag:
						if (damageMap == null) damageMap = new HashMap<>();
						return damageMap.computeIfAbsent(actualDamage(key), x -> mappingFunction.apply(key));
					case WildcardAll:
						T newValue = wildcard == null ? mappingFunction.apply(key) : wildcard;
						wildcard = newValue;
						hasWildcard = newValue != null;
						return newValue;
				}
			} finally {
				updateSize();
			}
			return null;
		}
	}

	private class SetView extends AbstractSet<Map.Entry<ItemStack, T>> {

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry<?, ?> entry)) return false;
			return java.util.Objects.equals(get(entry.getKey()), entry.getValue());
		}

		@Override
		public boolean add(Map.Entry<ItemStack, T> itemStackTEntry) {
			return itemStackTEntry.getKey() != null && itemStackTEntry.getValue() != null
					&& put(itemStackTEntry.getKey(), itemStackTEntry.getValue()) == null;
		}

		@Override
		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry<?, ?> entry)) return false;
			return ItemStackMap.this.remove(entry.getKey(), entry.getValue());
		}

		@Nonnull
		@Override
		public Iterator<Map.Entry<ItemStack, T>> iterator() {
			return Iterators.concat(Iterators.transform(itemMap.entrySet().iterator(), DetailIter::new));
		}

		@Override
		public int size() {
			return size;
		}
	}

	private enum KeyType {

		NotWildcard, // MetaMap
		WildcardMeta, // TagMap
		WildcardTag, // DamageMap
		WildcardAll, // WildCard
		;

		private static final KeyType[] VALUES = values();

		public KeyType withWildcardMeta() {
			return VALUES[ordinal() | 1];
		}

		public KeyType withWildcardTag() {
			return VALUES[ordinal() | 2];
		}
	}
}