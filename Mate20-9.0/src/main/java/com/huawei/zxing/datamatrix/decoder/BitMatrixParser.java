package com.huawei.zxing.datamatrix.decoder;

import com.huawei.zxing.FormatException;
import com.huawei.zxing.common.BitMatrix;

final class BitMatrixParser {
    private final BitMatrix mappingBitMatrix;
    private final BitMatrix readMappingMatrix;
    private final Version version;

    BitMatrixParser(BitMatrix bitMatrix) throws FormatException {
        int dimension = bitMatrix.getHeight();
        if (dimension < 8 || dimension > 144 || (dimension & 1) != 0) {
            throw FormatException.getFormatInstance();
        }
        this.version = readVersion(bitMatrix);
        this.mappingBitMatrix = extractDataRegion(bitMatrix);
        this.readMappingMatrix = new BitMatrix(this.mappingBitMatrix.getWidth(), this.mappingBitMatrix.getHeight());
    }

    /* access modifiers changed from: package-private */
    public Version getVersion() {
        return this.version;
    }

    private static Version readVersion(BitMatrix bitMatrix) throws FormatException {
        return Version.getVersionForDimensions(bitMatrix.getHeight(), bitMatrix.getWidth());
    }

    /* access modifiers changed from: package-private */
    public byte[] readCodewords() throws FormatException {
        int resultOffset;
        byte[] result = new byte[this.version.getTotalCodewords()];
        int resultOffset2 = 0;
        int row = 4;
        int column = 0;
        int numRows = this.mappingBitMatrix.getHeight();
        int numColumns = this.mappingBitMatrix.getWidth();
        boolean corner1Read = false;
        boolean corner2Read = false;
        boolean corner3Read = false;
        boolean corner4Read = false;
        while (true) {
            if (row == numRows && column == 0 && !corner1Read) {
                resultOffset = resultOffset2 + 1;
                result[resultOffset2] = (byte) readCorner1(numRows, numColumns);
                row -= 2;
                column += 2;
                corner1Read = true;
            } else if (row == numRows - 2 && column == 0 && (numColumns & 3) != 0 && !corner2Read) {
                resultOffset = resultOffset2 + 1;
                result[resultOffset2] = (byte) readCorner2(numRows, numColumns);
                row -= 2;
                column += 2;
                corner2Read = true;
            } else if (row == numRows + 4 && column == 2 && (numColumns & 7) == 0 && !corner3Read) {
                resultOffset = resultOffset2 + 1;
                result[resultOffset2] = (byte) readCorner3(numRows, numColumns);
                row -= 2;
                column += 2;
                corner3Read = true;
            } else if (row != numRows - 2 || column != 0 || (numColumns & 7) != 4 || corner4Read) {
                do {
                    if (row < numRows && column >= 0 && !this.readMappingMatrix.get(column, row)) {
                        result[resultOffset2] = (byte) readUtah(row, column, numRows, numColumns);
                        resultOffset2++;
                    }
                    row -= 2;
                    column += 2;
                    if (row < 0) {
                        break;
                    }
                } while (column < numColumns);
                int row2 = row + 1;
                int column2 = column + 3;
                do {
                    if (row2 >= 0 && column2 < numColumns && !this.readMappingMatrix.get(column2, row2)) {
                        result[resultOffset2] = (byte) readUtah(row2, column2, numRows, numColumns);
                        resultOffset2++;
                    }
                    row2 += 2;
                    column2 -= 2;
                    if (row2 >= numRows) {
                        break;
                    }
                } while (column2 >= 0);
                row = row2 + 3;
                column = column2 + 1;
                if (row < numRows && column >= numColumns) {
                    break;
                }
            } else {
                resultOffset = resultOffset2 + 1;
                result[resultOffset2] = (byte) readCorner4(numRows, numColumns);
                row -= 2;
                column += 2;
                corner4Read = true;
            }
            resultOffset2 = resultOffset;
            if (row < numRows) {
            }
        }
        if (resultOffset2 == this.version.getTotalCodewords()) {
            return result;
        }
        throw FormatException.getFormatInstance();
    }

    /* access modifiers changed from: package-private */
    public boolean readModule(int row, int column, int numRows, int numColumns) {
        if (row < 0) {
            row += numRows;
            column += 4 - ((numRows + 4) & 7);
        }
        if (column < 0) {
            column += numColumns;
            row += 4 - ((numColumns + 4) & 7);
        }
        this.readMappingMatrix.set(column, row);
        return this.mappingBitMatrix.get(column, row);
    }

    /* access modifiers changed from: package-private */
    public int readUtah(int row, int column, int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(row - 2, column - 2, numRows, numColumns)) {
            currentByte = 0 | 1;
        }
        int currentByte2 = currentByte << 1;
        if (readModule(row - 2, column - 1, numRows, numColumns)) {
            currentByte2 |= 1;
        }
        int currentByte3 = currentByte2 << 1;
        if (readModule(row - 1, column - 2, numRows, numColumns)) {
            currentByte3 |= 1;
        }
        int currentByte4 = currentByte3 << 1;
        if (readModule(row - 1, column - 1, numRows, numColumns)) {
            currentByte4 |= 1;
        }
        int currentByte5 = currentByte4 << 1;
        if (readModule(row - 1, column, numRows, numColumns)) {
            currentByte5 |= 1;
        }
        int currentByte6 = currentByte5 << 1;
        if (readModule(row, column - 2, numRows, numColumns)) {
            currentByte6 |= 1;
        }
        int currentByte7 = currentByte6 << 1;
        if (readModule(row, column - 1, numRows, numColumns)) {
            currentByte7 |= 1;
        }
        int currentByte8 = currentByte7 << 1;
        if (readModule(row, column, numRows, numColumns)) {
            return currentByte8 | 1;
        }
        return currentByte8;
    }

    /* access modifiers changed from: package-private */
    public int readCorner1(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte = 0 | 1;
        }
        int currentByte2 = currentByte << 1;
        if (readModule(numRows - 1, 1, numRows, numColumns)) {
            currentByte2 |= 1;
        }
        int currentByte3 = currentByte2 << 1;
        if (readModule(numRows - 1, 2, numRows, numColumns)) {
            currentByte3 |= 1;
        }
        int currentByte4 = currentByte3 << 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte4 |= 1;
        }
        int currentByte5 = currentByte4 << 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte5 |= 1;
        }
        int currentByte6 = currentByte5 << 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            currentByte6 |= 1;
        }
        int currentByte7 = currentByte6 << 1;
        if (readModule(2, numColumns - 1, numRows, numColumns)) {
            currentByte7 |= 1;
        }
        int currentByte8 = currentByte7 << 1;
        if (readModule(3, numColumns - 1, numRows, numColumns)) {
            return currentByte8 | 1;
        }
        return currentByte8;
    }

    /* access modifiers changed from: package-private */
    public int readCorner2(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 3, 0, numRows, numColumns)) {
            currentByte = 0 | 1;
        }
        int currentByte2 = currentByte << 1;
        if (readModule(numRows - 2, 0, numRows, numColumns)) {
            currentByte2 |= 1;
        }
        int currentByte3 = currentByte2 << 1;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte3 |= 1;
        }
        int currentByte4 = currentByte3 << 1;
        if (readModule(0, numColumns - 4, numRows, numColumns)) {
            currentByte4 |= 1;
        }
        int currentByte5 = currentByte4 << 1;
        if (readModule(0, numColumns - 3, numRows, numColumns)) {
            currentByte5 |= 1;
        }
        int currentByte6 = currentByte5 << 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte6 |= 1;
        }
        int currentByte7 = currentByte6 << 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte7 |= 1;
        }
        int currentByte8 = currentByte7 << 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            return currentByte8 | 1;
        }
        return currentByte8;
    }

    /* access modifiers changed from: package-private */
    public int readCorner3(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte = 0 | 1;
        }
        int currentByte2 = currentByte << 1;
        if (readModule(numRows - 1, numColumns - 1, numRows, numColumns)) {
            currentByte2 |= 1;
        }
        int currentByte3 = currentByte2 << 1;
        if (readModule(0, numColumns - 3, numRows, numColumns)) {
            currentByte3 |= 1;
        }
        int currentByte4 = currentByte3 << 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte4 |= 1;
        }
        int currentByte5 = currentByte4 << 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte5 |= 1;
        }
        int currentByte6 = currentByte5 << 1;
        if (readModule(1, numColumns - 3, numRows, numColumns)) {
            currentByte6 |= 1;
        }
        int currentByte7 = currentByte6 << 1;
        if (readModule(1, numColumns - 2, numRows, numColumns)) {
            currentByte7 |= 1;
        }
        int currentByte8 = currentByte7 << 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            return currentByte8 | 1;
        }
        return currentByte8;
    }

    /* access modifiers changed from: package-private */
    public int readCorner4(int numRows, int numColumns) {
        int currentByte = 0;
        if (readModule(numRows - 3, 0, numRows, numColumns)) {
            currentByte = 0 | 1;
        }
        int currentByte2 = currentByte << 1;
        if (readModule(numRows - 2, 0, numRows, numColumns)) {
            currentByte2 |= 1;
        }
        int currentByte3 = currentByte2 << 1;
        if (readModule(numRows - 1, 0, numRows, numColumns)) {
            currentByte3 |= 1;
        }
        int currentByte4 = currentByte3 << 1;
        if (readModule(0, numColumns - 2, numRows, numColumns)) {
            currentByte4 |= 1;
        }
        int currentByte5 = currentByte4 << 1;
        if (readModule(0, numColumns - 1, numRows, numColumns)) {
            currentByte5 |= 1;
        }
        int currentByte6 = currentByte5 << 1;
        if (readModule(1, numColumns - 1, numRows, numColumns)) {
            currentByte6 |= 1;
        }
        int currentByte7 = currentByte6 << 1;
        if (readModule(2, numColumns - 1, numRows, numColumns)) {
            currentByte7 |= 1;
        }
        int currentByte8 = currentByte7 << 1;
        if (readModule(3, numColumns - 1, numRows, numColumns)) {
            return currentByte8 | 1;
        }
        return currentByte8;
    }

    /* access modifiers changed from: package-private */
    public BitMatrix extractDataRegion(BitMatrix bitMatrix) {
        int symbolSizeRows;
        int j = this.version.getSymbolSizeRows();
        int symbolSizeColumns = this.version.getSymbolSizeColumns();
        if (bitMatrix.getHeight() == j) {
            int dataRegionSizeRows = this.version.getDataRegionSizeRows();
            int dataRegionSizeColumns = this.version.getDataRegionSizeColumns();
            int numDataRegionsRow = j / dataRegionSizeRows;
            int numDataRegionsColumn = symbolSizeColumns / dataRegionSizeColumns;
            BitMatrix bitMatrixWithoutAlignment = new BitMatrix(numDataRegionsColumn * dataRegionSizeColumns, numDataRegionsRow * dataRegionSizeRows);
            int dataRegionRow = 0;
            while (dataRegionRow < numDataRegionsRow) {
                int dataRegionRowOffset = dataRegionRow * dataRegionSizeRows;
                int dataRegionColumn = 0;
                while (dataRegionColumn < numDataRegionsColumn) {
                    int dataRegionColumnOffset = dataRegionColumn * dataRegionSizeColumns;
                    int i = 0;
                    while (i < dataRegionSizeRows) {
                        int readRowOffset = ((dataRegionSizeRows + 2) * dataRegionRow) + 1 + i;
                        int writeRowOffset = dataRegionRowOffset + i;
                        int j2 = 0;
                        while (true) {
                            symbolSizeRows = j;
                            int symbolSizeRows2 = j2;
                            if (symbolSizeRows2 >= dataRegionSizeColumns) {
                                break;
                            }
                            int symbolSizeColumns2 = symbolSizeColumns;
                            int readColumnOffset = ((dataRegionSizeColumns + 2) * dataRegionColumn) + 1 + symbolSizeRows2;
                            int dataRegionSizeRows2 = dataRegionSizeRows;
                            if (bitMatrix.get(readColumnOffset, readRowOffset)) {
                                int i2 = readColumnOffset;
                                bitMatrixWithoutAlignment.set(dataRegionColumnOffset + symbolSizeRows2, writeRowOffset);
                            }
                            j2 = symbolSizeRows2 + 1;
                            j = symbolSizeRows;
                            symbolSizeColumns = symbolSizeColumns2;
                            dataRegionSizeRows = dataRegionSizeRows2;
                        }
                        int dataRegionSizeRows3 = dataRegionSizeRows;
                        BitMatrix bitMatrix2 = bitMatrix;
                        i++;
                        j = symbolSizeRows;
                        dataRegionSizeRows = dataRegionSizeRows3;
                    }
                    int symbolSizeRows3 = j;
                    int i3 = symbolSizeColumns;
                    int dataRegionSizeRows4 = dataRegionSizeRows;
                    BitMatrix bitMatrix3 = bitMatrix;
                    dataRegionColumn++;
                    dataRegionSizeRows = dataRegionSizeRows4;
                }
                int symbolSizeRows4 = j;
                int i4 = symbolSizeColumns;
                int dataRegionSizeRows5 = dataRegionSizeRows;
                BitMatrix bitMatrix4 = bitMatrix;
                dataRegionRow++;
                dataRegionSizeRows = dataRegionSizeRows5;
            }
            int symbolSizeRows5 = j;
            int i5 = symbolSizeColumns;
            int i6 = dataRegionSizeRows;
            BitMatrix bitMatrix5 = bitMatrix;
            return bitMatrixWithoutAlignment;
        }
        BitMatrix bitMatrix6 = bitMatrix;
        int i7 = j;
        int i8 = symbolSizeColumns;
        throw new IllegalArgumentException("Dimension of bitMarix must match the version size");
    }
}
