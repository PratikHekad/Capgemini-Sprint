package com.notesapp.pages;

import com.notesapp.base.BasePage;
import org.openqa.selenium.By;

/**
 * NoteFormPage
 *
 * Handles the create/edit note form.
 * Same form is used for both operations, so one page object covers both.
 */
public class NoteFormPage extends BasePage {

    private final By categoryDropdown   = By.id("category");
    private final By titleField         = By.id("title");
    private final By descriptionField   = By.id("description");
    private final By saveButton         = By.xpath(
        "//button[@type='submit'] | //button[contains(text(),'Save') or contains(text(),'Create') or contains(text(),'Update')]");
    private final By titleValidationMsg = By.cssSelector("#title + .invalid-feedback, [data-testid='title-error']");
    private final By descValidationMsg  = By.cssSelector("#description + .invalid-feedback, [data-testid='description-error']");

    public NoteFormPage selectCategory(String category) {
        selectByText(categoryDropdown, category);
        return this;
    }

    public NoteFormPage enterTitle(String title) {
        type(titleField, title);
        return this;
    }

    public NoteFormPage enterDescription(String description) {
        type(descriptionField, description);
        return this;
    }

    /** Fill all three fields and save in one call. */
    public DashboardPage fillAndSave(String category, String title, String description) {
        selectCategory(category);
        enterTitle(title);
        enterDescription(description);
        click(saveButton);
        return new DashboardPage();
    }

    public NoteFormPage clickSave() {
        click(saveButton);
        return this;
    }

    public boolean isTitleValidationVisible()       { return isVisible(titleValidationMsg); }
    public boolean isDescriptionValidationVisible() { return isVisible(descValidationMsg); }
    public String  getTitleValidationMessage()      { return getText(titleValidationMsg); }
    public boolean isFormStillOpen()                { return isVisible(saveButton) || isVisible(titleField); }
}
