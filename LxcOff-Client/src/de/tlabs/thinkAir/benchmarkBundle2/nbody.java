package de.tlabs.thinkAir.benchmarkBundle2;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;

import android.util.Log;
import de.tlabs.thinkAir.benchmarkBundle1.Benchmark;

/* The Computer Language Benchmarks Game
 http://shootout.alioth.debian.org/

 contributed by Mark C. Lewis
 modified slightly by Chad Whipkey
 */

/**
 * Each program should model the orbits of Jovian planets, using the same simple
 * symplectic-integrator
 */
public final class nbody extends Remoteable implements Benchmark {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2751174071002858990L;

	private transient ExecutionController controller;

	public nbody(ExecutionController controller) {
		this.controller = controller;
	}

	public void main(int n) {
		NBodySystem bodies = new NBodySystem();
		System.out.printf("%.9f\n", bodies.energy());
		bodies = simulate(n, bodies);
		System.out.printf("%.9f\n", bodies.energy());
	}

	@SuppressWarnings("unused")
	@Remote
	private NBodySystem localsimulate(int n, NBodySystem bodies) {
		for (int i = 0; i < n; ++i)
			bodies.advance(0.01);
		return bodies;
	}

	@Override
	public void copyState(Remoteable arg0) {
		// TODO Auto-generated method stub

	}

	private NBodySystem simulate(int n, NBodySystem bodies) {
		Method toExecute;
		Class<?>[] paramTypes = { int.class, NBodySystem.class };
		Object[] paramValues = { n, bodies };
		NBodySystem result = null;
		try {
			toExecute = this.getClass().getDeclaredMethod("localsimulate",
					paramTypes);
			result = (NBodySystem) controller.execute(toExecute, paramValues,
					this);
		} catch (SecurityException e) {
			// Should never get here
			Log.e("PowerDroid-Client",
					"Problems with remoteable object implementation - reflection failures: "
							+ e);
		} catch (NoSuchMethodException e) {
			// Should never get here
			Log.e("PowerDroid-Client",
					"Problems with remoteable object implementation - reflection failures: "
							+ e);
		} catch (IllegalArgumentException e) {
			// Should never get here
			Log.e("PowerDroid-Client",
					"Problems with remoteable object implementation - reflection failures: "
							+ e);
		} catch (IllegalAccessException e) {
			// Should never get here
			Log.e("PowerDroid-Client",
					"Problems with remoteable object implementation - reflection failures: "
							+ e);
		} catch (ClassNotFoundException e) {
			// Should never get here
			Log.e("PowerDroid-Client",
					"Problems with remoteable object implementation - reflection failures: "
							+ e);
		} catch (Exception e) {
			// Must be an exception that happened in the original function
			e.printStackTrace();
		}
		return result;
	}
}

final class NBodySystem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1499012602773305205L;
	private Body[] bodies;

	public NBodySystem() {
		bodies = new Body[] { Body.sun(), Body.jupiter(), Body.saturn(),
				Body.uranus(), Body.neptune() };

		double px = 0.0;
		double py = 0.0;
		double pz = 0.0;
		for (int i = 0; i < bodies.length; ++i) {
			px += bodies[i].vx * bodies[i].mass;
			py += bodies[i].vy * bodies[i].mass;
			pz += bodies[i].vz * bodies[i].mass;
		}
		bodies[0].offsetMomentum(px, py, pz);
	}

	public void advance(double dt) {

		for (int i = 0; i < bodies.length; ++i) {
			Body iBody = bodies[i];
			for (int j = i + 1; j < bodies.length; ++j) {
				double dx = iBody.x - bodies[j].x;
				double dy = iBody.y - bodies[j].y;
				double dz = iBody.z - bodies[j].z;

				double dSquared = dx * dx + dy * dy + dz * dz;
				double distance = Math.sqrt(dSquared);
				double mag = dt / (dSquared * distance);

				iBody.vx -= dx * bodies[j].mass * mag;
				iBody.vy -= dy * bodies[j].mass * mag;
				iBody.vz -= dz * bodies[j].mass * mag;

				bodies[j].vx += dx * iBody.mass * mag;
				bodies[j].vy += dy * iBody.mass * mag;
				bodies[j].vz += dz * iBody.mass * mag;
			}
		}

		for (Body body : bodies) {
			body.x += dt * body.vx;
			body.y += dt * body.vy;
			body.z += dt * body.vz;
		}
	}

	public double energy() {
		double dx, dy, dz, distance;
		double e = 0.0;

		for (int i = 0; i < bodies.length; ++i) {
			Body iBody = bodies[i];
			e += 0.5
					* iBody.mass
					* (iBody.vx * iBody.vx + iBody.vy * iBody.vy + iBody.vz
							* iBody.vz);

			for (int j = i + 1; j < bodies.length; ++j) {
				Body jBody = bodies[j];
				dx = iBody.x - jBody.x;
				dy = iBody.y - jBody.y;
				dz = iBody.z - jBody.z;

				distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
				e -= (iBody.mass * jBody.mass) / distance;
			}
		}
		return e;
	}
}

final class Body implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5513399857340490660L;
	
	static final double PI = 3.141592653589793;
	static final double SOLAR_MASS = 4 * PI * PI;
	static final double DAYS_PER_YEAR = 365.24;

	public double x, y, z, vx, vy, vz, mass;

	public Body() {
	}

	static Body jupiter() {
		Body p = new Body();
		p.x = 4.84143144246472090e+00;
		p.y = -1.16032004402742839e+00;
		p.z = -1.03622044471123109e-01;
		p.vx = 1.66007664274403694e-03 * DAYS_PER_YEAR;
		p.vy = 7.69901118419740425e-03 * DAYS_PER_YEAR;
		p.vz = -6.90460016972063023e-05 * DAYS_PER_YEAR;
		p.mass = 9.54791938424326609e-04 * SOLAR_MASS;
		return p;
	}

	static Body saturn() {
		Body p = new Body();
		p.x = 8.34336671824457987e+00;
		p.y = 4.12479856412430479e+00;
		p.z = -4.03523417114321381e-01;
		p.vx = -2.76742510726862411e-03 * DAYS_PER_YEAR;
		p.vy = 4.99852801234917238e-03 * DAYS_PER_YEAR;
		p.vz = 2.30417297573763929e-05 * DAYS_PER_YEAR;
		p.mass = 2.85885980666130812e-04 * SOLAR_MASS;
		return p;
	}

	static Body uranus() {
		Body p = new Body();
		p.x = 1.28943695621391310e+01;
		p.y = -1.51111514016986312e+01;
		p.z = -2.23307578892655734e-01;
		p.vx = 2.96460137564761618e-03 * DAYS_PER_YEAR;
		p.vy = 2.37847173959480950e-03 * DAYS_PER_YEAR;
		p.vz = -2.96589568540237556e-05 * DAYS_PER_YEAR;
		p.mass = 4.36624404335156298e-05 * SOLAR_MASS;
		return p;
	}

	static Body neptune() {
		Body p = new Body();
		p.x = 1.53796971148509165e+01;
		p.y = -2.59193146099879641e+01;
		p.z = 1.79258772950371181e-01;
		p.vx = 2.68067772490389322e-03 * DAYS_PER_YEAR;
		p.vy = 1.62824170038242295e-03 * DAYS_PER_YEAR;
		p.vz = -9.51592254519715870e-05 * DAYS_PER_YEAR;
		p.mass = 5.15138902046611451e-05 * SOLAR_MASS;
		return p;
	}

	static Body sun() {
		Body p = new Body();
		p.mass = SOLAR_MASS;
		return p;
	}

	Body offsetMomentum(double px, double py, double pz) {
		vx = -px / SOLAR_MASS;
		vy = -py / SOLAR_MASS;
		vz = -pz / SOLAR_MASS;
		return this;
	}
}