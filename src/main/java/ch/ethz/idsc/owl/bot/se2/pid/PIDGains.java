// code by mcp
package ch.ethz.idsc.owl.bot.se2.pid;

import ch.ethz.idsc.tensor.Scalar;

// TODO MCP the 'I' part is missing
public class PIDGains {
  public final Scalar Kp;
  public final Scalar Kd;

  public PIDGains(Scalar Kp, Scalar Kd) {
    this.Kp = Kp;
    this.Kd = Kd;
    // TODO JPH how to GlobalAssert.that(Kd and Kd has correct unit);
  }
}
