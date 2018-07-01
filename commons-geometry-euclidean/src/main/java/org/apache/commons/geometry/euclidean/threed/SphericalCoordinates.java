/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.geometry.euclidean.threed;

import java.io.Serializable;

import org.apache.commons.geometry.core.Geometry;
import org.apache.commons.geometry.core.Spatial;
import org.apache.commons.geometry.core.util.Coordinates;
import org.apache.commons.geometry.core.util.SimpleCoordinateFormat;
import org.apache.commons.numbers.angle.PlaneAngleRadians;

/** Class representing a set of spherical coordinates in 3 dimensional Euclidean space.
 */
public class SphericalCoordinates implements Spatial, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 20180623L;

    /** Factory object for delegating instance creation. */
    private static final Coordinates.Factory3D<SphericalCoordinates> FACTORY = new Coordinates.Factory3D<SphericalCoordinates>() {

        /** {@inheritDoc} */
        @Override
        public SphericalCoordinates create(double a1, double a2, double a3) {
            return new SphericalCoordinates(a1, a2, a3);
        }
    };

    /** Radius value. */
    private final double radius;

    /** Azimuth angle in radians. */
    private final double azimuth;

    /** Polar angle in radians. */
    private final double polar;

    /** Simple constructor. The given inputs are normalized.
     * @param radius Radius value.
     * @param azimuth Azimuth angle in radians.
     * @param polar Polar angle in radians.
     */
    private SphericalCoordinates(double radius, double azimuth, double polar) {
        if (radius < 0) {
            // negative radius; flip the angles
            radius = Math.abs(radius);
            azimuth += Geometry.PI;
            polar += Geometry.PI;
        }

        if (Double.isFinite(azimuth) && (azimuth <= Geometry.MINUS_PI || azimuth > Geometry.PI)) {
            azimuth = PlaneAngleRadians.normalizeBetweenMinusPiAndPi(azimuth);

            // azimuth is now in the range [-pi, pi] but we want it to be in the range
            // (-pi, pi] in order to have completely unique coordinates
            if (azimuth <= -Geometry.PI) {
                azimuth += Geometry.TWO_PI;
            }
        }

        // normalize the polar angle; this is the angle between the polar vector and the point ray
        // so it is unsigned (unlike the azimuth) and should be in the range [0, pi]
        if (Double.isFinite(polar)) {
            polar = Math.abs(PlaneAngleRadians.normalizeBetweenMinusPiAndPi(polar));
        }

        this.radius = radius;
        this.azimuth = azimuth;
        this.polar = polar;
    }

    /** Return the radius value. The value is in the range {@code [0, +infinity)}.
     * @return the radius value
     */
    public double getRadius() {
        return radius;
    }

    /** Return the azimuth angle in radians. This is the angle in the x-y plane measured counter-clockwise from
     * the positive x axis. The angle is in the range {@code (-pi, pi]}.
     * @return the azimuth angle in radians
     */
    public double getAzimuth() {
        return azimuth;
    }

    /** Return the polar angle in radians. This is the angle the coordinate ray makes with the positive z axis.
     * The angle is in the range {@code [0, pi]}.
     * @return the polar angle in radians
     */
    public double getPolar() {
        return polar;
    }

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
        return 3;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isNaN() {
        return Double.isNaN(radius) || Double.isNaN(azimuth) || Double.isNaN(polar);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInfinite() {
        return !isNaN() && (Double.isInfinite(radius) || Double.isInfinite(azimuth) || Double.isInfinite(polar));
    }

    /** Convert this set of spherical coordinates to Cartesian coordinates.
     * The Cartesian coordinates are computed and passed to the given
     * factory instance. The factory's return value is returned.
     * @param factory Factory instance that will be passed the computed Cartesian coordinates
     * @return the value returned by the factory when passed Cartesian
     *      coordinates equivalent to this set of spherical coordinates.
     */
    public <T> T toCartesian(final Coordinates.Factory3D<T> factory) {
        return toCartesian(radius, azimuth, polar, factory);
    }

    /** Convert this set of spherical coordinates to a 3 dimensional vector.
     * @return A 3-dimensional vector with an equivalent set of
     *      coordinates.
     */
    public Vector3D toVector() {
        return toCartesian(Vector3D.getFactory());
    }

    /** Convert this set of spherical coordinates to a 3 dimensional point.
    * @return A 3-dimensional point with an equivalent set of
    *      coordinates.
    */
    public Point3D toPoint() {
        return toCartesian(Point3D.getFactory());
    }

    /**
     * Get a hashCode for this set of spherical coordinates.
     * <p>All NaN values have the same hash code.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        if (isNaN()) {
            return 127;
        }
        return 449 * (79 * Double.hashCode(radius) + Double.hashCode(azimuth) + Double.hashCode(polar));
    }

    /** Test for the equality of two sets of spherical coordinates.
     * <p>
     * If all values of two sets of coordinates are exactly the same, and none are
     * <code>Double.NaN</code>, the two sets are considered to be equal.
     * </p>
     * <p>
     * <code>NaN</code> values are considered to globally affect the coordinates
     * and be equal to each other - i.e, if either (or all) values of the
     * coordinate set are equal to <code>Double.NaN</code>, the set is equal to
     * {@link #NaN}.
     * </p>
     *
     * @param other Object to test for equality to this
     * @return true if two SphericalCoordinates objects are equal, false if
     *         object is null, not an instance of SphericalCoordinates, or
     *         not equal to this SphericalCoordinates instance
     *
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof SphericalCoordinates) {
            final SphericalCoordinates rhs = (SphericalCoordinates) other;
            if (rhs.isNaN()) {
                return this.isNaN();
            }

            return (radius == rhs.radius) && (azimuth == rhs.azimuth) && (polar == rhs.polar);
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return SimpleCoordinateFormat.getPointFormat().format(radius, azimuth, polar);
    }

    /** Create a {@link SphericalCoordinates} instance from the given values. The values are normalized
     * so that {@code radius} lies in the range {@code [0, +infinity)}, {@code azimuth} lies in the range
     * {@code (-pi, +pi]}, and {@code polar} lies in the range {@code [0, +pi]}.
     * @param radius the length of the line segment from the origin to the coordinate point.
     * @param azimuth the angle in the x-y plane, measured in radians counter-clockwise
     *      from the positive x-axis.
     * @param polar the angle in radians between the positive z-axis and the ray from the origin
     *      to the coordinate point.
     * @return a new {@link SphericalCoordinates} instance representing the same point as the given set of
     *      spherical coordinates.
     */
    public static SphericalCoordinates of(final double radius, final double azimuth, final double polar) {
        return new SphericalCoordinates(radius, azimuth, polar);
    }

    /** Convert the given set of Cartesian coordinates to spherical coordinates.
     * @param x X coordinate value
     * @param y Y coordinate value
     * @param z Z coordinate value
     * @return a set of spherical coordinates equivalent to the given Cartesian coordinates
     */
    public static SphericalCoordinates ofCartesian(final double x, final double y, final double z) {
        final double radius = Math.sqrt((x*x) + (y*y) + (z*z));
        final double azimuth = Math.atan2(y, x);

        // default the polar angle to 0 when the radius is 0
        final double polar = (radius > 0.0) ? Math.acos(z / radius) : 0.0;

        return new SphericalCoordinates(radius, azimuth, polar);
    }

    /** Parse the given string and return a new {@link SphericalCoordinates} instance. The parsed
     * coordinate values are normalized as in the {@link #of(double, double, double)} method.
     * The expected string format is the same as that returned by {@link #toString()}.
     * @param input the string to parse
     * @return new {@link SphericalCoordinates} instance
     * @throws IllegalArgumentException if the string format is invalid.
     */
    public static SphericalCoordinates parse(String input) {
        return SimpleCoordinateFormat.getPointFormat().parse(input, FACTORY);
    }

    /** Convert the given set of spherical coordinates to Cartesian coordinates.
     * The Cartesian coordinates are computed and passed to the given
     * factory instance. The factory's return value is returned.
     * @param radius The spherical radius value.
     * @param azimuth The spherical azimuth angle in radians.
     * @param polar The spherical polar angle in radians.
     * @param factory Factory instance that will be passed the
     * @return the value returned by the factory when passed Cartesian
     *      coordinates equivalent to the given set of spherical coordinates.
     */
    public static <T> T toCartesian(final double radius, final double azimuth, final double polar,
            Coordinates.Factory3D<T> factory) {
        final double xyLength = radius * Math.sin(polar);

        final double x = xyLength * Math.cos(azimuth);
        final double y = xyLength * Math.sin(azimuth);
        final double z = radius * Math.cos(polar);

        return factory.create(x, y, z);
    }

    /** Return a factory object for generating new {@link SphericalCoordinates} instances.
     * @return factory object for generating new instances.
     */
    public static Coordinates.Factory3D<SphericalCoordinates> getFactory() {
        return FACTORY;
    }
}
