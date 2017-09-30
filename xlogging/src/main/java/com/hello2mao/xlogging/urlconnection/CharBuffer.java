package com.hello2mao.xlogging.urlconnection;

public class CharBuffer {

    public char[] charArray;
    public int length;

    public CharBuffer(int n) {
        this.charArray = new char[n];
    }

    public final String subStringTrimmed(int sepratorIndex) {
        if (sepratorIndex > this.length) {
            throw new IndexOutOfBoundsException("endIndex: " + sepratorIndex + " > length: " + this.length);
        }
        if (sepratorIndex < 0) {
            throw new IndexOutOfBoundsException("beginIndex: 0 > endIndex: " + sepratorIndex);
        }
        int i;
        int lastIndex = sepratorIndex;
        for (i = 0; i < sepratorIndex; i++) {
            if (!isWhiteSpace(this.charArray[i])) {
                break;
            }
        }
        while ((lastIndex > i) && (isWhiteSpace(this.charArray[(lastIndex - 1)]))) {
            lastIndex -= 1;
        }
        return new String(this.charArray, i, lastIndex - i);
    }

    private static boolean isWhiteSpace(final char character) {
        return character == ' ' || character == '\t' || character == '\r' || character == '\n';
    }

    @Override
    public final String toString() {
        return new String(charArray, 0, length);
    }
}
