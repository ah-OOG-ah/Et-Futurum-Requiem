package ganymedes01.etfuturum.elytra;

import ganymedes01.etfuturum.elytra.math.ExpressionParser;

public class ModConfig {
    public static ModConfig INSTANCE;

    General general = new General();
    static class General {
        Banking banking = new Banking();
        static class Banking {
            boolean enable_banking = true;
            double banking_strength = 20.0;
            boolean simulate_control_surface_efficacy = false;
            boolean automatic_righting = false;
            double righting_strength = 50.0;
        }
    }

    AdvancedConfig advanced = new AdvancedConfig();
    static class AdvancedConfig {
        ExpressionParser banking_x_formula = new ExpressionParser("sin($roll * TO_RAD) * cos($pitch * TO_RAD) * 10 * $banking_strength");
        ExpressionParser banking_y_formula = new ExpressionParser("(-1 + cos($roll * TO_RAD)) * cos($pitch * TO_RAD) * 10 * $banking_strength");

        ExpressionParser elevator_efficacy_formula = new ExpressionParser("$velocity_x * $look_x + $velocity_y * $look_y + $velocity_z * $look_z");
        ExpressionParser aileron_efficacy_formula = new ExpressionParser("$velocity_x * $look_x + $velocity_y * $look_y + $velocity_z * $look_z");
        ExpressionParser rudder_efficacy_formula = new ExpressionParser("$velocity_x * $look_x + $velocity_y * $look_y + $velocity_z * $look_z");
    }

    public double getRightingStrength() {
        return general.banking.righting_strength;
    }

    public ExpressionParser getBankingXFormula() {
        return advanced.banking_x_formula;
    }

    public ExpressionParser getBankingYFormula() {
        return advanced.banking_y_formula;
    }

    public ExpressionParser getElevatorEfficacyFormula() {
        return advanced.elevator_efficacy_formula;
    }

    public ExpressionParser getAileronEfficacyFormula() {
        return advanced.aileron_efficacy_formula;
    }

    public ExpressionParser getRudderEfficacyFormula() {
        return advanced.rudder_efficacy_formula;
    }

    public double getBankingStrength() {
        return general.banking.banking_strength;
    }
}
