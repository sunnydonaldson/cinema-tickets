package uk.gov.dwp.uc.pairtest.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

public class TicketTypeRequestTest {
  @Test
  public void getPrice_forAdult_returnsCorrectPrice() {
    final int price = Type.ADULT.getPrice();

    assertEquals(20, price);
  }

  @Test
  public void getPrice_forChild_returnsCorrectPrice() {
    final int price = Type.CHILD.getPrice();

    assertEquals(10, price);
  } 

  @Test
  public void getPrice_forInfant_returnsCorrectPrice() {
    final int price = Type.INFANT.getPrice();

    assertEquals(0, price);
  }

  @Test
  public void getTotalCost_returnsCorrectCost() {
    TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 3);

    final int total = request.getTotalcost();

    assertEquals(60, total);
  }
}
