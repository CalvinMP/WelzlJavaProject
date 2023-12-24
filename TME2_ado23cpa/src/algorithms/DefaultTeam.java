package algorithms;
import java.awt.Point;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import supportGUI.Circle;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;


public class DefaultTeam {
	//Choisir entre l'algorithle naïf et celui de Welzl pour calculer le 
	// plus petit cercle englobant une liste de n points

	public double testWelzl(ArrayList<Point> points){
		long startTime = 0;
		startTime = System.nanoTime();
		Welzl(points, new ArrayList<Point>() );

		return (double)(System.nanoTime() - startTime) / 1_000_000;
	}

	public double testNaif(ArrayList<Point> points){
		long startTime = 0;
		startTime = System.nanoTime();
		cercleMinNaif(points);
		return (double)(System.nanoTime() - startTime) / 1_000_000;
	}

	public void algorithmsTest(ArrayList<Point> points){

		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
		DecimalFormat decimalFormat = new DecimalFormat("#.#####", symbols);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("./algo_tests.csv") ) ) {
			for (int i = 0; i < 882; i++){
				double welzlTime = testWelzl(points);
				double naiveTime = testNaif(points);
				writer.write(String.format("Welzl, %s, Naif, %s", decimalFormat.format(welzlTime), decimalFormat.format(naiveTime) ) + System.lineSeparator() );
				//writer.write(String.format("Naif, %d, %s", points.size(), decimalFormat.format(naiveTime) ) + System.lineSeparator() );

				try {
					Robot robot = new Robot();

					robot.keyPress(KeyEvent.VK_R);
					robot.keyRelease(KeyEvent.VK_R);

				} catch (AWTException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Circle calculCercleMin(ArrayList<Point> points) {
		//algorithmsTest(points);
		//System.out.println("Both tests done.");
		//return cercleMinNaif(points);
		return Welzl(points, new ArrayList<Point>());
	}

	//Algorithme naïf 
	private Circle cercleMinNaif(ArrayList<Point> inputPoints) {
		ArrayList<Point> points = (ArrayList<Point>) inputPoints.clone();
		if (points.size()<1) return null;
		double cX,cY,cRadius,cRadiusSquared;
		for (Point p: points){
			for (Point q: points){
				cX = .5*(p.x+q.x);
				cY = .5*(p.y+q.y);
				cRadiusSquared = 0.25*((p.x-q.x)*(p.x-q.x)+(p.y-q.y)*(p.y-q.y));
				boolean allHit = true;
				for (Point s: points)
					if ((s.x-cX)*(s.x-cX)+(s.y-cY)*(s.y-cY)>cRadiusSquared){
						allHit = false;
						break;
					}
				if (allHit) return new Circle(new Point((int)cX,(int)cY),(int)Math.sqrt(cRadiusSquared));
			}
		}
		double resX=0;
		double resY=0;
		double resRadiusSquared=Double.MAX_VALUE;
		for (int i=0;i<points.size();i++){
			for (int j=i+1;j<points.size();j++){
				for (int k=j+1;k<points.size();k++){
					Point p=points.get(i);
					Point q=points.get(j);
					Point r=points.get(k);
					//si les trois sont colineaires on passe
					if ((q.x-p.x)*(r.y-p.y)-(q.y-p.y)*(r.x-p.x)==0) continue;
					//si p et q sont sur la meme ligne, ou p et r sont sur la meme ligne, on les echange
					if ((p.y==q.y)||(p.y==r.y)) {
						if (p.y==q.y){
							p=points.get(k); //ici on est certain que p n'est sur la meme ligne de ni q ni r
							r=points.get(i); //parce que les trois points sont non-colineaires
						} else {
							p=points.get(j); //ici on est certain que p n'est sur la meme ligne de ni q ni r
							q=points.get(i); //parce que les trois points sont non-colineaires
						}
					}
					//on cherche les coordonnees du cercle circonscrit du triangle pqr
					//soit m=(p+q)/2 et n=(p+r)/2
					double mX=.5*(p.x+q.x);
					double mY=.5*(p.y+q.y);
					double nX=.5*(p.x+r.x);
					double nY=.5*(p.y+r.y);
					//soit y=alpha1*x+beta1 l'equation de la droite passant par m et perpendiculaire a la droite (pq)
					//soit y=alpha2*x+beta2 l'equation de la droite passant par n et perpendiculaire a la droite (pr)
					double alpha1=(q.x-p.x)/(double)(p.y-q.y);
					double beta1=mY-alpha1*mX;
					double alpha2=(r.x-p.x)/(double)(p.y-r.y);
					double beta2=nY-alpha2*nX;
					//le centre c du cercle est alors le point d'intersection des deux droites ci-dessus
					cX=(beta2-beta1)/(double)(alpha1-alpha2);
					cY=alpha1*cX+beta1;
					cRadiusSquared=(p.x-cX)*(p.x-cX)+(p.y-cY)*(p.y-cY);
					if (cRadiusSquared>=resRadiusSquared) continue;
					boolean allHit = true;
					for (Point s: points)
						if ((s.x-cX)*(s.x-cX)+(s.y-cY)*(s.y-cY)>cRadiusSquared){
							allHit = false;
							break;
						}
					if (allHit) {System.out.println("Found r="+Math.sqrt(cRadiusSquared));resX=cX;resY=cY;resRadiusSquared=cRadiusSquared;}
				}
			}
		}
		return new Circle(new Point((int)resX,(int)resY),(int)Math.sqrt(resRadiusSquared));
	}

	//Algorithme de Welzl 
	public Circle Welzl(ArrayList<Point> P, ArrayList<Point> R) {
		ArrayList<Point> P1 = new ArrayList<Point>(P);
		Random rand = new Random(); // point au hasard
		Circle d = new Circle(new Point(0, 0), 0);
		if (P1.isEmpty() || R.size() == 3) {
			d = trivial(new ArrayList<Point>(), R); // trois points sont sur le cercle, cas trivial
		} else {
			Point pt = P1.get(rand.nextInt(P1.size() ) );
			P1.remove(pt); // retirer le point choisi de P
			d = Welzl(P1, R); // appel récursif sur P1 et R , avec P1 = P-{p}
			if (d != null && !contains(d, pt)) { // si D est bien défini et il ne contient pas P
				R.add(pt); // Ajouter p à R
				d = Welzl(P1, R);
				R.remove(pt);
			}
		}
		return d;
	}


	public Circle trivial(ArrayList<Point> P, ArrayList<Point> R) {
		if (P.isEmpty() && R.size() == 0)
			return new Circle(new Point(0, 0), 0);
		Circle D = new Circle(new Point(0, 0), 0);
		if (R.size() == 1) {
			D = new Circle(R.get(0), 0); // Un point : cercle de centre ce point là
		}
		if (R.size() == 2) { // Deux points, cercle de diamètre ces deux points là
			double cx = (R.get(0).x + R.get(1).x) / 2;
			double cy = (R.get(0).y + R.get(1).y) / 2;
			double d = R.get(0).distance(R.get(1)) / 2;
			Point p = new Point((int) cx, (int) cy);
			D = new Circle(p, (int) Math.ceil(d));
		} else {
			if (R.size() == 3) // 3 points, il suffit de calculer le cercle circonscrit
				D = circle3point(R.get(0), R.get(1), R.get(2));
		}
		return D;
	}

	// Calcule la norme d'un vecteur
	private int norme(Point a) {
		return (a.x * a.x) + (a.y * a.y);
	}

	// Calcul le cercle circonscrit de 3 points
	private Circle circle3point(Point a, Point b, Point c) { // Calcul du cercle circonscrit des trois points
		double d = (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) * 2;
		if (d == 0)
			return new Circle(new Point(0, 0), 0);
		double x = ((norme(a) * (b.y - c.y)) + (norme(b) * (c.y - a.y)) + (norme(c) * (a.y - b.y))) / d;
		double y = ((norme(a) * (c.x - b.x)) + (norme(b) * (a.x - c.x)) + (norme(c) * (b.x - a.x))) / d;
		Point p = new Point((int) x, (int) y);
		return new Circle(p, (int) Math.ceil(p.distance(a))); // Ce cercle est donc le résultat !
	}

	public Boolean contains(Circle cercle, Point point) { // Permet de vérifier su un cercle "cercle" englobe un point
		// "point"
		if ((cercle != null) && (point != null)) {
			if (point.distance(cercle.getCenter()) <= cercle.getRadius())
				return true;
		}
		return false;
	}
}