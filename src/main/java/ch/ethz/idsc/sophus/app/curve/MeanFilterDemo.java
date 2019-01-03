// code by jph
package ch.ethz.idsc.sophus.app.curve;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

import javax.swing.JTextField;
import javax.swing.JToggleButton;

import ch.ethz.idsc.owl.gui.GraphicsUtil;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.owl.math.planar.CurvatureComb;
import ch.ethz.idsc.sophus.app.api.AbstractDemo;
import ch.ethz.idsc.sophus.app.api.ControlPointsDemo;
import ch.ethz.idsc.sophus.app.api.DubinsGenerator;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplay;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplays;
import ch.ethz.idsc.sophus.app.util.SpinnerLabel;
import ch.ethz.idsc.sophus.curve.BSpline4CurveSubdivision;
import ch.ethz.idsc.sophus.curve.BezierFunction;
import ch.ethz.idsc.sophus.filter.GeodesicMeanFilter;
import ch.ethz.idsc.sophus.group.Se2CoveringGeodesic;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Subdivide;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.red.Nest;
import ch.ethz.idsc.tensor.sca.Clip;

/* package */ class MeanFilterDemo extends ControlPointsDemo {
  private static final Scalar COMB_SCALE = DoubleScalar.of(1); // .5 (1 for presentation)
  private static final Color COLOR_CURVATURE_COMB = new Color(0, 0, 0, 128);
  // ---
  private final SpinnerLabel<Integer> spinnerRadius = new SpinnerLabel<>();
  private final JToggleButton jToggleCtrl = new JToggleButton("ctrl");
  private final JToggleButton jToggleBndy = new JToggleButton("bndy");
  private final JToggleButton jToggleComb = new JToggleButton("comb");
  private final JToggleButton jToggleLine = new JToggleButton("line");

  MeanFilterDemo() {
    super(true, GeodesicDisplays.ALL);
    {
      Tensor blub = Tensors.fromString("{{1,0,0},{2,0,2.5708},{1,0,2.1},{1.5,0,0},{2.3,0,-1.2},{1.5,0,0}}");
      setControl(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
          Tensor.of(blub.stream().map(row -> row.pmul(Tensors.vector(2, 1, 1))))));
    }
    JTextField jTextField = new JTextField(10);
    jTextField.setPreferredSize(new Dimension(100, 28));
    timerFrame.jToolBar.add(jTextField);
    // ---
    jToggleCtrl.setSelected(true);
    timerFrame.jToolBar.add(jToggleCtrl);
    // ---
    jToggleBndy.setSelected(true);
    timerFrame.jToolBar.add(jToggleBndy);
    // ---
    jToggleComb.setSelected(true);
    timerFrame.jToolBar.add(jToggleComb);
    // ---
    jToggleLine.setSelected(false);
    timerFrame.jToolBar.add(jToggleLine);
    // ---
    spinnerRadius.setList(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    spinnerRadius.setValue(9);
    spinnerRadius.addToComponentReduced(timerFrame.jToolBar, new Dimension(50, 28), "refinement");
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    GraphicsUtil.setQualityHigh(graphics);
    Tensor control = control();
    int radius = spinnerRadius.getValue();
    renderControlPoints(geometricLayer, graphics);
    GeodesicDisplay geodesicDisplay = geodesicDisplay();
    TensorUnaryOperator geodesicMeanFilter = GeodesicMeanFilter.of(geodesicDisplay.geodesicInterface(), radius);
    Tensor refined = geodesicMeanFilter.apply(control);
    Tensor curve = Nest.of(BSpline4CurveSubdivision.of(geodesicDisplay.geodesicInterface())::string, refined, 7);
    if (jToggleLine.isSelected()) {
      Tensor linear = Subdivide.of(Clip.unit(), 1 << 8) //
          .map(BezierFunction.of(Se2CoveringGeodesic.INSTANCE, control));
      graphics.setColor(new Color(0, 255, 0, 128));
      Path2D path2d = geometricLayer.toPath2D(linear);
      graphics.draw(path2d);
    }
    {
      graphics.setColor(Color.BLUE);
      Path2D path2d = geometricLayer.toPath2D(curve);
      graphics.setStroke(new BasicStroke(1.25f));
      graphics.draw(path2d);
      graphics.setStroke(new BasicStroke(1f));
    }
    if (jToggleComb.isSelected()) {
      graphics.setColor(COLOR_CURVATURE_COMB);
      Path2D path2d = geometricLayer.toPath2D(CurvatureComb.of(refined, COMB_SCALE, false));
      graphics.draw(path2d);
    }
    renderPoints(geometricLayer, graphics, refined);
  }

  public static void main(String[] args) {
    AbstractDemo abstractDemo = new MeanFilterDemo();
    abstractDemo.timerFrame.jFrame.setBounds(100, 100, 1000, 600);
    abstractDemo.timerFrame.jFrame.setVisible(true);
  }
}