// code by ynager
package ch.ethz.idsc.owl.bot.se2.glc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.win.AffineTransforms;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.sophus.lie.se2.Se2Matrix;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.mat.DiagonalMatrix;

/** Renders an arbitrary image at the supplier state */
/* package */ class EntityImageRender implements RenderInterface {
  private final Tensor matrix;
  private final Supplier<StateTime> supplier;
  private final BufferedImage bufferedImage;

  public EntityImageRender(Supplier<StateTime> supplier, BufferedImage bufferedImage, Tensor range) {
    this.supplier = supplier;
    this.bufferedImage = bufferedImage;
    Tensor scale = Tensors.vector(bufferedImage.getWidth(), bufferedImage.getHeight()) //
        .pmul(range.map(Scalar::reciprocal));
    Tensor invsc = DiagonalMatrix.of( //
        +scale.Get(0).reciprocal().number().doubleValue(), //
        -scale.Get(1).reciprocal().number().doubleValue(), 1);
    // not generic
    Tensor translate = Se2Matrix.translation( //
        Tensors.vector(-bufferedImage.getWidth() / 3, -bufferedImage.getHeight() / 2));
    matrix = invsc.dot(translate);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    geometricLayer.pushMatrix(Se2Matrix.of(supplier.get().state()));
    graphics.drawImage(bufferedImage, AffineTransforms.toAffineTransform(geometricLayer.getMatrix().dot(matrix)), null);
    geometricLayer.popMatrix();
  }
}
