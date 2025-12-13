@SessionAttributes("formData")
public class TestController {


    /**
     * Initialize formData model attribute for the session
     */
    @ModelAttribute("formData")
    public FormData getFormData() {
        return new FormData();
    }


   

    /**
     * Display the form page (preview mode)
     *
     * @param pageId   The page identifier (e.g., "P1.1", "P1.2", etc.)
     * @param formData The form data from session
     * @param model    Spring MVC model
     * @return Thymeleaf template name
     */
    @GetMapping("/forms/preview/{sectionId}/{pageId}")
    public String showForm(@PathVariable String sectionId,
                           @PathVariable String pageId,
                           @ModelAttribute("formData") FormData formData,
                           Model model) {
        // Set current page to maintain state
        formData.setCurrentPage(pageId);
        formData.setCurrentSection(sectionId);

        // Return the appropriate Thymeleaf template based on pageId
        return String.format("forms/%s/%s", sectionId, pageId);

    }
