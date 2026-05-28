package com.notesapp.pages;

import com.notesapp.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardPage
 *
 * The notes list page after login.
 * Use this when you need finer control over individual note cards
 * (edit button, per-card operations).
 * For most tests use NotesPage which is simpler.
 */
public class DashboardPage extends BasePage {

    private final By addNoteButton = By.xpath(
        "//a[contains(@href,'/notes/new')] | //button[contains(text(),'Add Note')] | //*[@data-testid='add-note-btn']");
    private final By noteCards     = By.cssSelector(".card, [data-testid='note-card']");
    private final By noteTitles    = By.cssSelector(".card-title, [data-testid='note-title']");
    private final By logoutButton  = By.xpath("//a[contains(text(),'Logout')] | //button[contains(text(),'Logout')]");
    private final By userGreeting  = By.cssSelector(".navbar-text, [data-testid='user-name']");
    private final By successToast  = By.cssSelector(".alert-success, .toast-success, [data-testid='alert-message']");

    public NoteFormPage clickAddNote() {
        click(addNoteButton);
        return new NoteFormPage();
    }

    public NoteFormPage clickEditOnNote(String noteTitle) {
        WebElement card = findCardByTitle(noteTitle);
        card.findElement(By.xpath(".//a[contains(@href,'/edit')] | .//button[contains(text(),'Edit')]")).click();
        return new NoteFormPage();
    }

    public DashboardPage clickDeleteOnNote(String noteTitle) {
        WebElement card = findCardByTitle(noteTitle);
        card.findElement(By.xpath(".//button[contains(text(),'Delete')] | .//*[@data-testid='note-delete']")).click();
        return this;
    }

    public DashboardPage confirmDelete() {
        By confirmBtn = By.xpath("//button[contains(text(),'Confirm') or contains(text(),'Yes') or contains(text(),'Delete')]");
        if (isPresent(confirmBtn)) click(confirmBtn);
        return this;
    }

    public void logout() { click(logoutButton); }

    public boolean isNoteVisible(String title) {
        return driver.findElements(noteTitles).stream()
                .anyMatch(el -> el.getText().trim().equals(title));
    }

    public boolean isLoggedIn() {
        return isVisible(userGreeting) || isPresent(logoutButton);
    }

    public int getNoteCount() {
        return driver.findElements(noteCards).size();
    }

    public List<String> getAllNoteTitles() {
        return driver.findElements(noteTitles).stream()
                .map(el -> el.getText().trim())
                .collect(Collectors.toList());
    }

    private WebElement findCardByTitle(String title) {
        return waitForAllVisible(noteCards).stream()
                .filter(card -> {
                    try {
                        return card.findElement(
                            By.cssSelector(".card-title, [data-testid='note-title']"))
                            .getText().trim().equals(title);
                    } catch (Exception e) { return false; }
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Card not found: " + title));
    }
}
