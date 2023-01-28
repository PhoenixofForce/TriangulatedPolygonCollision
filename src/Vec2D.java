import java.awt.*;

public class Vec2D {

	public double x, y;

	public Vec2D() {
		this.x = 0.0;
		this.y = 0.0;
	}

	public Vec2D(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vec2D(Polygon p, int n) {
		this.x = p.xpoints[n];
		this.y = p.ypoints[n];
	}

	public Vec2D add(Vec2D v) {
		this.x += v.x;
		this.y += v.y;

		return this;
	}

	public Vec2D add(double x, double y) {
		this.x += x;
		this.y += y;

		return this;
	}

	public Vec2D sub(Vec2D v) {
		this.x -= v.x;
		this.y -= v.y;

		return this;
	}

	public Vec2D sub(double x, double y) {
		this.x -= x;
		this.y -= y;

		return this;
	}

	public Vec2D mult(double r) {
		this.x *= r;
		this.y *= r;

		return this;
	}

	public Vec2D div(double r) {
		this.x /= r;
		this.y /= r;

		return this;
	}

	public double scalar(Vec2D second) {
		return second.x * x + second.y * y;
	}

	public double cross(Vec2D v) {
		return this.x * v.y - this.y * v.x;
	}

	public double distanceTo(Vec2D v) {
		return Math.sqrt(Math.pow(this.x - v.x, 2) + Math.pow(this.y - v.y, 2));
	}

	public double length() {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	public Vec2D normalize() {
		double l = length();
		if(l != 0) div(l);
		else {
			x = 0;
			y = 0;
		}
		return this;
	}

	public Vec2D normalize(int norm) {
		double l = length();
		if(l != 0) div(l);
		else {
			x = 0;
			y = 0;
		}
		mult(norm);
		return this;
	}

	public double angle() {
		return angle(new Vec2D(1, 0));
	}

	public double angle(Vec2D second) {
		return Math.toDegrees(Math.acos(scalar(second) / (length() * second.length())));
	}

	/**
	 *
	 * @param b
	 * @return
	 */
	public boolean kollinear(Vec2D b) {
		double r = b.x / x;
		return r == b.y/ y;
	}

	@Override
	public Vec2D clone() {
		return new Vec2D(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vec2D) {
			Vec2D b = (Vec2D) obj;
			return x == b.x && y == b.y;
		}
		return false;
	}

	@Override
	public int hashCode() {
		double hash = 17L;
		hash = hash*31 + x;
		hash = hash*31 + y;

		Long l = new Double(hash).longValue();
		return l.intValue();
	}

	@Override
	public String toString() {
		return String.format("(%f | %f)", x, y);
	}



	public static Vec2D angleToVec(double deg) {
		return new Vec2D(Math.cos(Math.toRadians(deg)), Math.sin(Math.toRadians(deg)));
	}
}
