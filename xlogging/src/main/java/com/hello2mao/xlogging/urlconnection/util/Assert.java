package com.hello2mao.xlogging.urlconnection.util;

/**
 * FIXME:该类是否需要有待确定
 */
public class Assert {
    private static boolean isEnabled;

    static {
        Assert.isEnabled = true;
    }

    public static void fail(final Throwable throwable) {
        if (!Assert.isEnabled) {
            return;
        }
        throw new Error("Assertion failed", throwable);
    }

    public static void is(final boolean trueOrFalse) {
        if (Assert.isEnabled && !trueOrFalse) {
            throw new Error("Assertion failed");
        }
    }

    public static void notNull(final Object object) {
        if (Assert.isEnabled && object == null) {
            throw new Error("Not null assertion failed");
        }
    }
}
