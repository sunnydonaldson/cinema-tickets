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
  public void getTotalCost_adult_returnsCorrectCost() {
    TicketTypeRequest request = new TicketTypeRequest(Type.ADULT, 3);

    final int total = request.getTotalCost();

    assertEquals(60, total);
  }

  @Test
  public void getTotalCost_child_returnsCorrectCost() {
    TicketTypeRequest request = new TicketTypeRequest(Type.CHILD, 4);

    final int total = request.getTotalCost();

    assertEquals(40, total);
  }

  @Test
  public void getTotalCost_infant_returnsCorrectCost() {
    TicketTypeRequest request = new TicketTypeRequest(Type.INFANT, 10);

    final int total = request.getTotalCost();
    
    assertEquals(0, total);
  }
}
