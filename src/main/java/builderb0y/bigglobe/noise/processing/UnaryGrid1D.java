package builderb0y.bigglobe.noise.processing;

import builderb0y.bigglobe.noise.Grid1D;
import builderb0y.bigglobe.noise.NumberArray;

public interface UnaryGrid1D extends UnaryGrid, Grid1D {

	@Override
	public abstract Grid1D getGrid();

	@Override
	public default double getValue(long seed, int x) {
		return this.operate(this.getGrid().getValue(seed, x));
	}

	@Override
	public default void getBulkX(long seed, int startX, NumberArray samples) {
		this.getGrid().getBulkX(seed, startX, samples);
		this.operate(samples);
	}
}