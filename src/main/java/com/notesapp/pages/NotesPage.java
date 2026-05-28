package com.notesapp.pages;

import com.notesapp.base.BasePage;
import com.notesapp.utils.WaitUtil;
import org.openqa.selenium.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * NotesPage
 *
 * Main notes dashboard page.
 * Handles adding, finding, and deleting note cards.
 */
public class NotesPage extends BasePage {

    // ── Locators ─────────────────────────────────────────────────────────────

    // "+" / Add Note button
    private final By addNoteBtn       = By.xpath("//a[contains(@href,'/notes/new')] | //button[contains(text(),'Add Note')] | //*[contains(text(),'Add Note')] | //*[@data-testid='add-note-btn'] | //*[@data-testid='add-note']");

    // Form fields (create / edit)
    private final By categoryDropdown = By.id("category");
    private final By titleField       = By.id("title");
    private final By descriptionField = By.id("description");
    private final By saveNoteBtn      = By.cssSelector("[data-testid='note-submit'], #saveNote, .modal button[type='submit']");

    // Note cards on the dashboard
    private final By noteCards        = By.cssSelector(".card, [class*='note-item'], [data-testid='note']");

    // Inner elements found RELATIVE to each card
    private final By cardTitle        = By.cssSelector(".card-title, [class*='title'], h5");
    private final By cardCategory     = By.cssSelector(".badge, [class*='category']");
    private final By cardDescription  = By.cssSelector(".card-text, [class*='description'], p");

    // Edit / Delete flow
    private final By editBtn          = By.xpath(".//a[contains(@href,'/edit')] | .//button[contains(text(),'Edit')] | .//*[@data-testid='note-edit'] | .//*[contains(@class,'fa-edit')] | .//*[contains(@class,'fa-pencil')]");
    private final By deleteBtn        = By.xpath(".//button[contains(text(),'Delete')] | .//*[contains(@class,'delete')] | .//*[@data-testid='note-delete'] | .//*[@data-testid='delete-note'] | .//*[contains(@class,'fa-trash')]");
    private final By confirmDeleteBtn = By.xpath("//div[contains(@class,'modal')]//button[contains(@class,'btn-danger') or contains(text(),'Delete') or contains(text(),'Confirm') or @data-testid='note-delete-confirm'] | //*[@data-testid='confirm-delete']");

    // Feedback
    private final By successToast    = By.cssSelector(".alert-success, [class*='success']");
    private final By validationError = By.xpath("//*[contains(@class,'invalid-feedback')] | //*[contains(@class,'alert-danger')] | //*[@data-testid='title-error'] | //*[@data-testid='description-error'] | //*[contains(@class,'error-msg')] | //*[contains(@data-testid,'error')]");

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Full create-note flow: click Add → fill form → save → wait for list update. */
    public NotesPage addNote(String category, String title, String description) {
        log.info("Adding note — Category: {}, Title: '{}'", category, title);
        click(addNoteBtn);
        waitForVisible(categoryDropdown);
        try { Thread.sleep(500); } catch (Exception ignored) {}
        selectByText(categoryDropdown, category);
        type(titleField, title);
        type(descriptionField, description);
        click(saveNoteBtn);
        WaitUtil.waitForPageReady();
        return this;
    }

    /** Opens the modal and saves without filling anything. Triggers validation. */
    public NotesPage submitBlankNote() {
        log.info("Submitting blank note (negative test)");
        click(addNoteBtn);
        waitForVisible(categoryDropdown); // Wait for modal transition to complete stably
        try { Thread.sleep(500); } catch (Exception ignored) {}
        click(saveNoteBtn);
        return this;
    }

    /** Opens the modal, fills only description, leaves title blank. */
    public NotesPage submitNoteWithoutTitle(String category, String description) {
        log.info("Submitting note without title (negative test)");
        click(addNoteBtn);
        waitForVisible(categoryDropdown); // Wait for modal transition to complete stably
        try { Thread.sleep(500); } catch (Exception ignored) {}
        selectByText(categoryDropdown, category);
        type(descriptionField, description);
        click(saveNoteBtn);
        return this;
    }

    /** Finds the card by title and clicks Delete. Returns true if found. */
    public boolean deleteNoteByTitle(String title) {
        try {
            int deletedCount = 0;
            while (true) {
                WebElement card = null;
                try {
                    By cardLocator = By.xpath("//*[contains(@class,'card') or contains(@class,'note-item') or @data-testid='note-card'][descendant::*[contains(text(),'" + title + "')]]");
                    card = driver.findElement(cardLocator);
                } catch (NoSuchElementException e) {
                    break;
                }
                log.info("Found note card for '{}' — clicking delete (iteration {})", title, deletedCount + 1);
                WebElement cardDeleteBtn = card.findElement(deleteBtn);
                click(cardDeleteBtn);
                click(confirmDeleteBtn);
                deletedCount++;
                try { Thread.sleep(800); } catch (Exception ignored) {}
            }
            log.info("Finished delete loop for '{}'. Total deleted: {}", title, deletedCount);
            WaitUtil.waitForPageReady();
            return true;
        } catch (Exception e) {
            log.error("Failed to delete note '{}'", title, e);
            try {
                String bodyText = driver.findElement(By.tagName("body")).getText();
                log.error("Entire body text on delete failure:\n{}", bodyText);
            } catch (Exception ignored) {}
            return false;
        }
    }

    /**
     * TC-UI-03 — Edit an existing note by title.
     * Finds the note card, clicks Edit, updates title + description, and saves.
     * Returns true if the edit flow completed without error.
     */
    public boolean editNoteByTitle(String oldTitle, String newTitle, String newDescription) {
        try {
            log.info("Editing note '{}' → new title: '{}'", oldTitle, newTitle);
            By cardLocator = By.xpath(
                "//*[contains(@class,'card') or contains(@class,'note-item') or @data-testid='note-card']"
                + "[descendant::*[contains(text(),'" + oldTitle + "')]]");
            WebElement card = driver.findElement(cardLocator);
            WebElement cardEditBtn = card.findElement(editBtn);
            click(cardEditBtn);

            // Wait for the form/modal to open
            waitForVisible(titleField);
            try { Thread.sleep(400); } catch (Exception ignored) {}

            // Clear and retype title
            WebElement titleEl = driver.findElement(titleField);
            titleEl.clear();
            titleEl.sendKeys(newTitle);

            // Clear and retype description
            WebElement descEl = driver.findElement(descriptionField);
            descEl.clear();
            descEl.sendKeys(newDescription);

            click(saveNoteBtn);
            WaitUtil.waitForPageReady();
            log.info("Note edited successfully to '{}'", newTitle);
            return true;
        } catch (Exception e) {
            log.error("Failed to edit note '{}'", oldTitle, e);
            return false;
        }
    }

    // ── State checks ─────────────────────────────────────────────────────────

    public boolean isNotePresent(String title) {
        try {
            return wait.until(d -> {
                List<WebElement> elements = d.findElements(By.xpath("//*[contains(text(),'" + title + "')]"));
                log.info("isNotePresent polling — Found {} elements containing target '{}'", elements.size(), title);
                for (WebElement el : elements) {
                    String t = el.getAttribute("textContent");
                    log.info("Found element textContent: '{}'", t);
                    if (t != null && t.trim().equalsIgnoreCase(title)) return true;
                }
                return false;
            });
        } catch (Exception e) {
            log.error("isNotePresent timed out for title: {}", title);
            try {
                String bodyText = driver.findElement(By.tagName("body")).getText();
                log.error("Entire body text on failure:\n{}", bodyText);
            } catch (Exception ignored) {}
            return false;
        }
    }

    public int getNoteCount() {
        return driver.findElements(noteCards).size();
    }

    public boolean isSuccessMessageVisible() {
        try {
            waitForVisible(successToast);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidationErrorVisible() {
        try {
            return wait.until(d -> {
                hideCookieConsent();
                List<WebElement> elements = d.findElements(validationError);
                for (WebElement el : elements) {
                    if (el.isDisplayed() && !el.getText().trim().isEmpty()) {
                        log.info("Found visible validation error: '{}'", el.getText());
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            log.error("Validation error check failed. Current URL: {}", getCurrentUrl());
            return false;
        }
    }

    public String getValidationErrorText() {
        try {
            List<WebElement> elements = driver.findElements(validationError);
            for (WebElement el : elements) {
                if (el.isDisplayed()) {
                    String text = el.getText().trim();
                    if (!text.isEmpty()) return text;
                    text = el.getAttribute("textContent").trim();
                    if (!text.isEmpty()) return text;
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    public List<String> getAllNoteTitles() {
        return driver.findElements(noteCards).stream()
                .map(card -> {
                    try { return card.findElement(cardTitle).getText().trim(); }
                    catch (Exception e) { return ""; }
                })
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }

    public boolean waitForNoteToDisappear(String title) {
        try {
            return wait.until(d -> {
                hideCookieConsent();
                List<WebElement> elements = d.findElements(By.xpath("//*[contains(text(),'" + title + "')]"));
                for (WebElement el : elements) {
                    try {
                        String t = el.getAttribute("textContent");
                        if (t != null && t.trim().equalsIgnoreCase(title) && el.isDisplayed()) {
                            log.info("Note '{}' is still visible", title);
                            return false; // Still present and visible
                        }
                    } catch (StaleElementReferenceException ignored) {}
                }
                log.info("Note '{}' is no longer visible", title);
                return true; // Not present or not visible
            });
        } catch (Exception e) {
            log.warn("Timed out waiting for note '{}' to disappear", title);
            return false;
        }
    }
}
