import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HitBox {

	private List<Polygon> hb;
	private Polygon outline;

	public HitBox(Polygon p) {
		outline = p;
		hb = Triangulation.triangulate(p);	//always triangulate the polygon
	}

	public HitBox(Polygon p, boolean skipTriangulation) {
		outline = p;

		if(skipTriangulation) {
			hb = new ArrayList<>();
			hb.add(outline);
		} else {
			hb = Triangulation.triangulate(p);
		}
	}

	public boolean collidesWithOne(List<HitBox> boxes) {
		for (HitBox b : boxes) {
			if (b != this && b.collides(this)) return true;
		}
		return false;
	}

	public boolean collides(HitBox b2) {
		if(boundingCollision(b2)) {	//first check bounding box for better performance
			for(Polygon p1: hb) {
				for(Polygon p2: b2.hb) {
					if(satCollision(p1, p2)) return true;
				}
			}
		}
		return false;
	}

	private boolean boundingCollision(HitBox b2) {
		return boundingBox().intersects(b2.boundingBox());
	}

	/**
	 * SAT = Separating Axis Theorem
	 * "Two closed convex objects are disjoint iff there exists a line ("separating axis") onto which the two objects' projections are disjoint."
	 *   ~<a href="https://en.wikipedia.org/wiki/Hyperplane_separation_theorem">Wikipedia</a>
	 *
	 * In my words: if two convex polygons overlap, you can draw a line between them and vice versa.
	 * The axis that test the separation are the sides of the polygons
	 *
	 * @param firstPolygon
	 * @param secondPolygon
	 * @return
	 */
	private boolean satCollision(Polygon firstPolygon, Polygon secondPolygon) {
		List<Vec2D> axis = new ArrayList<>();

		//get sides from the first polygon
		for (int i = 0; i < firstPolygon.npoints; i++) {
			int dx = firstPolygon.xpoints[(i + 1) % firstPolygon.npoints] - firstPolygon.xpoints[i];
			int dy = firstPolygon.ypoints[(i + 1) % firstPolygon.npoints] - firstPolygon.ypoints[i];

			Vec2D toAdd = new Vec2D(dx, dy);

			boolean shouldSkip = false;
			for (Vec2D v : axis) if (v.kollinear(toAdd)) shouldSkip = true;
			if(shouldSkip) continue;

			axis.add(toAdd);
		}

		//get sides from second polygon
		for (int i = 0; i < secondPolygon.npoints; i++) {
			int dx = secondPolygon.xpoints[(i + 1) % secondPolygon.npoints] - secondPolygon.xpoints[i];
			int dy = secondPolygon.ypoints[(i + 1) % secondPolygon.npoints] - secondPolygon.ypoints[i];

			Vec2D toAdd = new Vec2D(dx, dy);

			boolean shouldSkip = false;
			for (Vec2D v : axis) if (v.kollinear(toAdd)) shouldSkip = true;
			if(shouldSkip) continue;

			axis.add(toAdd);
		}

		/*     +------------+ <- axis
		 *     #    +----+
		 *     #    |    |
		 *     #    |    |
		 *     #    +----+
		 *  *->|
		 *     #            +----+
		 *     #            |    |
		 *     #            +----+
		 *     |
		 *  + <- projection plane (90° rotated axis)
		 *
		 *  * at this place the two polygons do not overlap, that means
		 *    the axis can be placed here and will separate the polygons
		 *    that means the two polygons do NOT overlap.
		 * 	  if there is all planes have an overlap, the two polygons overlap
		 *
		 */
		for(Vec2D a: axis) {
			//turns axis by 90 degree
			Vec2D projectionPlane = new Vec2D(-a.y, a.x);

			double firstPolygonPlaneProjectionStart = Integer.MAX_VALUE;
			double firstPolygonPlaneProjectionEnd = Integer.MIN_VALUE;	//Double.MIN_VALUE is not the smallest double, so i use Integer instead

			double secondPolygonPlaneProjectionStart = Integer.MAX_VALUE;
			double secondPolygonPlaneProjectionEnd = Integer.MIN_VALUE;

			for (int i = 0; i < firstPolygon.npoints; i++) {
				Vec2D point = new Vec2D(firstPolygon.xpoints[i], firstPolygon.ypoints[i]);

				double projectionOntoPlane = point.scalar(projectionPlane);
				firstPolygonPlaneProjectionStart = Math.min(firstPolygonPlaneProjectionStart, projectionOntoPlane);
				firstPolygonPlaneProjectionEnd = Math.max(firstPolygonPlaneProjectionEnd, projectionOntoPlane);
			}

			for (int i = 0; i < secondPolygon.npoints; i++) {
				Vec2D point = new Vec2D(secondPolygon.xpoints[i], secondPolygon.ypoints[i]);

				double projectionOntoPlane = point.scalar(projectionPlane);
				secondPolygonPlaneProjectionStart = Math.min(secondPolygonPlaneProjectionStart, projectionOntoPlane);
				secondPolygonPlaneProjectionEnd = Math.max(secondPolygonPlaneProjectionEnd, projectionOntoPlane);
			}

			boolean firstPolygonBeforeSecond = firstPolygonPlaneProjectionEnd <= secondPolygonPlaneProjectionStart;
			boolean firstPolygonAfterSecond = firstPolygonPlaneProjectionStart >= secondPolygonPlaneProjectionEnd;
			if(firstPolygonBeforeSecond || firstPolygonAfterSecond) return false;
		}

		return true;
	}

	public Rectangle boundingBox() {
		int smallX = Integer.MAX_VALUE;
		int smallY = Integer.MAX_VALUE;
		int bigX = Integer.MIN_VALUE;
		int bigY = Integer.MIN_VALUE;

		for(int i = 0; i < outline.npoints; i++) {
			if(outline.xpoints[i] < smallX) smallX = outline.xpoints[i];
			if(outline.xpoints[i] > bigX) bigX = outline.xpoints[i];
			if(outline.ypoints[i] < smallY) smallY = outline.ypoints[i];
			if(outline.ypoints[i] > bigY) bigY = outline.ypoints[i];
		}

		return new Rectangle(smallX, smallY, bigX - smallX, bigY - smallY);
	}

	public List<Polygon> getPolygons() {
		return hb;
	}

	public Polygon getOutline() {
		return outline;
	}
}