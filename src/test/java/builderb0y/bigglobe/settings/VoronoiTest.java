package builderb0y.bigglobe.settings;

import java.util.random.RandomGenerator;

import org.junit.jupiter.api.Test;

import builderb0y.bigglobe.noise.Permuter;
import builderb0y.bigglobe.settings.VoronoiDiagram2D.Cell;
import builderb0y.bigglobe.settings.VoronoiDiagram2D.SeedPoint;
import builderb0y.bigglobe.util.Derivative2D;

import static org.junit.jupiter.api.Assertions.*;

public class VoronoiTest {

	@Test
	public void testDerivatives() {
		VoronoiDiagram2D diagram = new VoronoiDiagram2D(new Seed(12345L), 256, 192);
		Derivative2D derivative = new Derivative2D();
		RandomGenerator random = new Permuter(678910);
		for (int trial = 0; trial < 16384; trial++) {
			int rng = random.nextInt();
			int x = (int)(short)(rng);
			int z = rng >> 16;
			Cell centerCell = diagram.getNearestCell(x, z);
			centerCell.derivativeProgressToEdgeSquaredD(derivative, x, z);
			assertEquals(derivative.value, centerCell.progressToEdgeSquaredD(x, z), 1.0D / 65536.0D);

			SeedPoint adjacentSeedPoint;

			adjacentSeedPoint = diagram.getNearestSeedPoint(x + 1, z, centerCell.center);
			if (centerCell.center.cellEquals(adjacentSeedPoint)) {
				double adjacentValue = centerCell.progressToEdgeSquaredD(x + 1, z);
				check(derivative.value, +derivative.dx, adjacentValue);
			}

			adjacentSeedPoint = diagram.getNearestSeedPoint(x - 1, z, centerCell.center);
			if (centerCell.center.cellEquals(adjacentSeedPoint)) {
				double adjacentValue = centerCell.progressToEdgeSquaredD(x - 1, z);
				check(derivative.value, -derivative.dx, adjacentValue);
			}

			adjacentSeedPoint = diagram.getNearestSeedPoint(x, z + 1, centerCell.center);
			if (centerCell.center.cellEquals(adjacentSeedPoint)) {
				double adjacentValue = centerCell.progressToEdgeSquaredD(x, z + 1);
				check(derivative.value, +derivative.dy, adjacentValue);
			}

			adjacentSeedPoint = diagram.getNearestSeedPoint(x, z - 1, centerCell.center);
			if (centerCell.center.cellEquals(adjacentSeedPoint)) {
				double adjacentValue = centerCell.progressToEdgeSquaredD(x, z - 1);
				check(derivative.value, -derivative.dy, adjacentValue);
			}
		}
	}

	public static void check(double current, double diff, double adjacent) {
		assertEquals(current + diff, adjacent, 1.0D / 1024.0D);
	}
}