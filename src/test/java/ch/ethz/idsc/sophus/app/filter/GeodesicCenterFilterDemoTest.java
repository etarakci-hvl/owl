// code by jph
package ch.ethz.idsc.sophus.app.filter;

import ch.ethz.idsc.sophus.app.api.AbstractDemoHelper;
import junit.framework.TestCase;

public class GeodesicCenterFilterDemoTest extends TestCase {
  public void testSimple() {
    AbstractDemoHelper.brief(new GeodesicCenterFilterDemo());
  }
}