import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Triangulation {

	/** Triangulation by Ear clipping
	 *
	 * @param polygon that should be triangulated
	 * @return a list of triangles that form the gives polygon
	 */
	public static List<Polygon> triangulate(Polygon polygon) {
		List<Polygon> triangulatedPolygon = new ArrayList<>();

		Polygon truncatedPolygon = polygon;
		int i = 0;

		while(truncatedPolygon.npoints > 3) {
			while(i < 0) i += truncatedPolygon.npoints;

			int lastPoint = (i - 1) % truncatedPolygon.npoints;
			int thisPoint = i;
			int nextPoint = (i + 1) % truncatedPolygon.npoints;
			while(lastPoint < 0) lastPoint += truncatedPolygon.npoints;

			double angleInDegrees;
			{//calculate angleInDegrees ( lastPoint thisPoint nextPoint ) in degrees
				double a = Math.atan2(truncatedPolygon.ypoints[nextPoint] - truncatedPolygon.ypoints[thisPoint], truncatedPolygon.xpoints[nextPoint] - truncatedPolygon.xpoints[thisPoint]);
				double b = Math.atan2(truncatedPolygon.ypoints[lastPoint] - truncatedPolygon.ypoints[thisPoint], truncatedPolygon.xpoints[lastPoint] - truncatedPolygon.xpoints[thisPoint]);

				angleInDegrees = a - b;
				if(a < b) angleInDegrees += 2 * Math.PI;
				angleInDegrees = Math.toDegrees(angleInDegrees);
			}

			if(angleInDegrees > 180) {	//if current triangle is not convex, try again with the next one
				i = (i + 1)%truncatedPolygon.npoints;
				continue;
			}

			Vec2D lastPointAsVec = new Vec2D(truncatedPolygon, lastPoint);
			Vec2D nextPointAsVec = new Vec2D(truncatedPolygon, nextPoint);

			Vec2D rayCastResult = castRayTo(truncatedPolygon, lastPointAsVec, nextPointAsVec);
			boolean isLineBetweenPoints = !rayCastResult.equals(nextPointAsVec);
			if(isLineBetweenPoints) {	//if there is already a polygon obscuring the path from the last to the next Point continue with the next point
				i = (i + 1) % truncatedPolygon.npoints;
				continue;
			}

			Polygon p = new Polygon();
			p.addPoint(truncatedPolygon.xpoints[lastPoint], truncatedPolygon.ypoints[lastPoint]);
			p.addPoint(truncatedPolygon.xpoints[thisPoint], truncatedPolygon.ypoints[thisPoint]);
			p.addPoint(truncatedPolygon.xpoints[nextPoint], truncatedPolygon.ypoints[nextPoint]);
			triangulatedPolygon.add(p);

			//remove the current point from the polygon
			Polygon newBody = new Polygon();
			for(int j = 0; j < truncatedPolygon.npoints; j++) {
				if(j != i) newBody.addPoint(truncatedPolygon.xpoints[j], truncatedPolygon.ypoints[j]);
			}
			truncatedPolygon = newBody;

			i--;
		}

		Polygon p =  new Polygon();
		p.addPoint(truncatedPolygon.xpoints[0], truncatedPolygon.ypoints[0]);
		p.addPoint(truncatedPolygon.xpoints[1], truncatedPolygon.ypoints[1]);
		p.addPoint(truncatedPolygon.xpoints[2], truncatedPolygon.ypoints[2]);
		triangulatedPolygon.add(p);

		return triangulatedPolygon;
	}

	/**
	 * Casts a ray from a given start point in a given direction, and calculates intersection with given obstacles
	 * The function calculates the intersection of a polygon by calculating the intersection with each side
	 * The function calculates intersection of a side by calculating the intersection of two rays
	 *   - the first ray is the sideDirection from the firstPoint to the secondPoint
	 *   - the second ray is the given ray
	 *  Also look at <a href="https://github.com/PhoenixofForce/RayCast-Scene">My RayCast scene</a>
	 *
	 *                  +-------+ p
	 *                  |       |
	 *                  |   ^   |
	 *                  |  /    |
	 *  firstPoint ->   +-?----->   <- secondPoint
	 *                   /      ^
	 *                  /       | sideDirection
	 *         dir ->  /
	 *               * <- start
	 *
	 *   ? - possible intersection
	 *   p - current polygon
	 *
	 * @return point of intersection, or destination if no intersection was found
	 */
	private static Vec2D castRayTo(Polygon p, Vec2D start, Vec2D dest) {
		Vec2D s = dest.clone().sub(start);

		List<Vec2D> possibleDestinations = new ArrayList<>();
		possibleDestinations.add(dest);

		for(int i = 0; i < p.npoints; i++) {
			Vec2D a = new Vec2D(p.xpoints[i], p.ypoints[i]);
			Vec2D b = new Vec2D(p.xpoints[(i+1)%p.npoints], p.ypoints[(i+1)%p.npoints]);

			Vec2D r = b.clone().sub(a);

			double t = start.clone().sub(a).cross(s) / r.cross(s);
			double u = a.clone().sub(start).cross(r) / s.cross(r);

			 /*  Line-Line intersection between Ray and the side
				  let i be the point of intersection then
					- t, denotes the distance of i from the firstPoint (in direction of secondPoint) such that firstPoint + sideDirection * t = i
					- u, denotes the distance of i from the start (in the given direction dir) such that start + dir * u = i
				  The following conditions must be met, such the intersection lies on the current side
					- u >= 0, if u would be smaller than 0, the given direction would be reversed
					- 0 <= t <= 1, t values outside this range, would mean an intersection outside the given side
			 */
			if(u >= 0.0f && u <= 1.0f && t <= 1.0f && t >= 0.0f) {
				double x  = Math.round(start.x + u * s.x);
				double y  = Math.round(start.y + u * s.y);
				possibleDestinations.add(new Vec2D(x, y));
			}
		}

		Vec2D nearestDestination = dest;
		double distance = dest.distanceTo(start);

		for(Vec2D i: possibleDestinations) {
			if(i.equals(start)) continue;
			double d = i.distanceTo(start);
			if(d < distance) {
				distance = d;
				nearestDestination = i;
			}
		}

		return nearestDestination;
	}
}
