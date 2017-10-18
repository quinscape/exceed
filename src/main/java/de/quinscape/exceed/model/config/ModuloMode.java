package de.quinscape.exceed.model.config;

public enum ModuloMode
{
    //The remainder is positive if the dividend is negative, otherwise it is negative.
    ROUND_UP,
    //The remainder has the same sign as the dividend. This uses 'truncating division' and matches the behaviour of JavaScript's remainder operator %.
    ROUND_DOWN,
    //The remainder has the same sign as the divisor. This matches Python's % operator.
    ROUND_FLOOR,
    //The IEEE 754 remainder function.
    ROUND_HALF_EVEN,
    EUCLID;

    public static ModuloMode getDefault()
    {
        return ROUND_DOWN;
    }
}
