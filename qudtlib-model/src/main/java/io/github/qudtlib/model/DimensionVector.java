package io.github.qudtlib.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the QUDT dimension vector and allows for converting between a dimension vector IRI and
 * the numeric values, as well as for some manipulations.
 *
 * <p>Note that the last value, the 'D' dimension is special: it is only an indicator that the
 * dimension vector represents a ratio (causing all other dimensions to cancel each other out). It
 * never changes by multiplication, and its value is only 1 iff all other dimensions are 0.
 */
public class DimensionVector {
    private static final char[] dimensions = new char[] {'A', 'E', 'L', 'I', 'M', 'H', 'T', 'D'};

    public static DimensionVector DIMENSIONLESS =
            new DimensionVector(new int[] {0, 0, 0, 0, 0, 0, 0, 1});

    private String dimensionVectorIri;

    private final int[] values;

    public static Optional<DimensionVector> of(String dimensionVectorIri) {
        try {
            return Optional.of(new DimensionVector(dimensionVectorIri));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static DimensionVector ofRequired(String dimensionVectorIri) {
        return new DimensionVector(dimensionVectorIri);
    }

    public static Optional<DimensionVector> of(int[] dimensionValues) {
        try {
            return Optional.of(new DimensionVector(dimensionValues));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static DimensionVector ofRequired(int[] dimensionValues) {
        return new DimensionVector(dimensionValues);
    }

    public DimensionVector(String dimensionVectorIri) {
        this.dimensionVectorIri = dimensionVectorIri;
        String localName = dimensionVectorIri.substring(dimensionVectorIri.lastIndexOf("/") + 1);
        int[] dimValues = new int[8];
        String[] numbers = localName.split("[^\\-\\d]");
        String[] indicators = localName.split("-?\\d{1,2}");
        if (indicators.length != 8) {
            throw new RuntimeException(
                    String.format(
                            "Cannot process dimension vector iri %s: unexpected number of dimensions: %d",
                            dimensionVectorIri, numbers.length));
        }
        for (int i = 0; i < indicators.length; i++) {

            if (indicators[i].charAt(0) != dimensions[i]) {
                throw new RuntimeException(
                        String.format(
                                "Expected dimension indicator '%s', encountered '%s'",
                                dimensions[i], indicators[i]));
            }
            dimValues[i] =
                    Integer.parseInt(numbers[i + 1]); // split produces an empty first array element
        }
        this.values = dimValues;
    }

    public DimensionVector(int[] dimensionValues) {
        if (dimensionValues.length != 8) {
            throw new RuntimeException(
                    "wrong dimensionality, expected 8, got " + dimensionValues.length);
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            sb.append(dimensions[i]).append(dimensionValues[i]);
        }
        this.values = dimensionValues;
        this.dimensionVectorIri = "http://qudt.org/vocab/dimensionvector/" + sb.toString();
    }

    public DimensionVector() {
        this(new int[8]);
    }

    public boolean isDimensionless() {
        return this.equals(DIMENSIONLESS);
    }

    public String getDimensionVectorIri() {
        return dimensionVectorIri;
    }

    public int[] getValues() {
        return values;
    }

    public DimensionVector multiply(int by) {
        int[] mult = new int[8];
        boolean isRatio = true;
        for (int i = 0; i < 7; i++) {
            mult[i] = this.values[i] * by;
            if (mult[i] != 0) {
                isRatio = false;
            }
        }
        setRatio(mult, isRatio);
        return new DimensionVector(mult);
    }

    private void setRatio(int[] values, boolean isRatio) {
        values[7] = isRatio ? 1 : 0;
    }

    public DimensionVector combine(DimensionVector other) {
        int[] combined = new int[8];
        boolean isRatio = true;
        for (int i = 0; i < 8; i++) {
            combined[i] = this.values[i] + other.getValues()[i];
            if (combined[i] != 0) {
                isRatio = false;
            }
        }
        setRatio(combined, isRatio);
        return new DimensionVector(combined);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DimensionVector)) return false;
        DimensionVector that = (DimensionVector) o;
        return Objects.equals(getDimensionVectorIri(), that.getDimensionVectorIri())
                && Arrays.equals(getValues(), that.getValues());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getDimensionVectorIri());
        result = 31 * result + Arrays.hashCode(getValues());
        return result;
    }
}
