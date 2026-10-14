package com.sportsbooking.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Five critical user journeys of the Sports Facility Booking System (Task 9 test plan, TC-01 to TC-05).
 */
class UserJourneysUiIT extends BaseUiTest {

    private static final String TOMORROW = LocalDate.now().plusDays(1).toString();
    private static final String DAY_AFTER = LocalDate.now().plusDays(2).toString();

    @Test
    @DisplayName("TC-01 New player can register and lands on the facilities page")
    void tc01_registerNewPlayer() {
        register("Tanisha Sharma", uniqueEmail("tanisha"), "9876543210");

        assertThat(flashMessage()).contains("Registration successful");
        assertThat(find("#nav-user").getText()).isEqualTo("Hi, Tanisha Sharma");
        assertThat(driver.getTitle()).startsWith("Facilities");
    }

    @Test
    @DisplayName("TC-02 Invalid registration shows clear error messages")
    void tc02_invalidRegistrationShowsErrors() {
        register("Asha", "not-an-email", "123");

        assertThat(find("#email-error").getText()).isEqualTo("Enter a valid email address");
        assertThat(find("#phone-error").getText()).isEqualTo("Phone must be 10 digits");
    }

    @Test
    @DisplayName("TC-03 Player books a free slot and sees it as REQUESTED in My Bookings")
    void tc03_bookSlotAndTrackStatus() {
        register("Ravi Kumar", uniqueEmail("ravi"), "9876543211");

        String time = bookFirstFreeSlot(2, TOMORROW);

        assertThat(flashMessage()).contains("Booking requested").contains(time);
        find("#nav-my-bookings").click();
        assertThat(find("#bookings-table").getText()).contains("Football Turf", time, "REQUESTED");
    }

    @Test
    @DisplayName("TC-04 A slot booked by one player cannot be booked by another (no double booking)")
    void tc04_doubleBookingIsBlocked() {
        register("Player One", uniqueEmail("one"), "9000000001");
        String time = bookFirstFreeSlot(3, TOMORROW);
        signOut();

        register("Player Two", uniqueEmail("two"), "9000000002");
        open("/slots?facilityId=3&date=" + TOMORROW);
        String row = find("#slots-table").getText();
        assertThat(row).contains(time + " Booked");

        // Try to force the booking anyway by submitting another slot's form with the taken slot id
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "const f=document.querySelector(\"button[id^='book-']\").form;"
                        + "f.querySelector('input[name=slotId]').value=arguments[0];f.submit();",
                takenSlotId(time));
        assertThat(flashError()).contains("already booked");
    }

    @Test
    @DisplayName("TC-05 Manager confirms a request, then the player cancels it and the slot is free again")
    void tc05_confirmThenCancel() {
        String email = uniqueEmail("meera");
        String player = "Meera " + email.substring(6, 14);   // unique name, so the manager confirms this booking
        register(player, email, "9000000003");
        String time = bookFirstFreeSlot(1, DAY_AFTER);
        signOut();

        signIn(MANAGER_EMAIL);
        find("#nav-manager").click();
        find("#requests-table");
        driver.findElements(org.openqa.selenium.By.cssSelector("#requests-table tbody tr")).stream()
                .filter(r -> r.getText().contains(player))
                .findFirst().orElseThrow()
                .findElement(org.openqa.selenium.By.cssSelector("button[id^='confirm-']")).click();
        assertThat(flashMessage()).contains("confirmed");
        signOut();

        signIn(email);
        find("#nav-my-bookings").click();
        assertThat(find("#bookings-table").getText()).contains("CONFIRMED");
        find("button[id^='cancel-']").click();
        assertThat(flashMessage()).contains("cancelled");
        assertThat(find("#bookings-table").getText()).contains("CANCELLED");

        open("/slots?facilityId=1&date=" + DAY_AFTER);
        assertThat(find("#slots-table").getText()).contains(time + " Available");
    }

    private String takenSlotId(String time) {
        return driver.findElements(org.openqa.selenium.By.cssSelector("#slots-table tbody tr")).stream()
                .filter(r -> r.getText().startsWith(time))
                .findFirst().orElseThrow()
                .getAttribute("id").replace("slot-", "");
    }
}
