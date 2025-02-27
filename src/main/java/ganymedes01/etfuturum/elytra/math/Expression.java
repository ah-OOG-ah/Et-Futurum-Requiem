package ganymedes01.etfuturum.elytra.math;

import java.util.Map;

public interface Expression {
    double eval(Map<String, Double> vars);
}
