package com.hello2mao.xlogging.io;

public class CharBuffer {

    public char[] charArray;
    public int length;

    public CharBuffer(int bufferSize) {
        this.charArray = new char[bufferSize];
    }

    public String subStringTrimmed(int separateIndex) {
        if (separateIndex > length) {
            throw new IndexOutOfBoundsException("endIndex: " + separateIndex
                    + " > length: " + length);
        }
        if (separateIndex < 0) {
            throw new IndexOutOfBoundsException("beginIndex: 0 > endIndex: " + separateIndex);
        }
        int i;
        int lastIndex = separateIndex;
        for (i = 0; i < separateIndex; i++) {
            if (!isWhiteSpace(charArray[i])) {
                break;
            }
        }
        while ((lastIndex > i) && (isWhiteSpace(charArray[(lastIndex - 1)]))) {
            lastIndex -= 1;
        }
        return new String(charArray, i, lastIndex - i);
    }

    private static boolean isWhiteSpace(char character) {
        return character == ' ' || character == '\t' || character == '\r' || character == '\n';
    }

    @Override
    public final String toString() {
        return new String(charArray, 0, length);
    }
}
