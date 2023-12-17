(require 'ert)
(load-file "../gimel.el")

(ert-deftest test-gimel-path-append ()
  (should (string= "/foo/bar/baloo" (gimel-path-append "/foo" "bar" "baloo")))
  (should (string= "foo/bar/baloo" (gimel-path-append "foo" "bar" "baloo")))
  (should (string= "/foo/bar/baloo" (gimel-path-append "/foo" "/bar/" "//baloo"))))

(ert-deftest test-gimel-split-org-content-valid ()
  "Test the `gimel-split-org-content` function with valid data."
  (with-temp-buffer
    (org-mode)
    (insert "#+title: Some Content\n#+category: A category\n\n* Header\nsome content\n* Another Header\nmore content")
    (let ((result (gimel-split-org-content)))
      (should (equal (car result) "#+title: Some Content\n#+category: A category\n\n"))
      (should (equal (cadr result) "* Header\nsome content\n* Another Header\nmore content")))))

(ert-deftest test-gimel-split-org-content-invalid ()
  "Test the `gimel-split-org-content` function with invalid data."
  (with-temp-buffer
    (org-mode)
    (insert "#title: Some Content\n#+category: A category\n\nDonec pretium posuere tellus.")
    (should-error (gimel-split-org-content))))

(ert-deftest test-gimel-org-metadata-to-yaml ()
  "Test `gimel-org-metadata-to-yaml` function."
  (should (string= "---\ntitle: \"Some Content\"\ncategory: \"A category\"\n---"
                   (gimel-org-metadata-to-yaml "#+title: Some Content\n#+category: A category\n\n"))))

(ert-deftest test-gimel-generate-id-from-heading ()
  "Testing `gimel-generate-id-from-heading` function."
  (should (string= "-heading-1"
                   (gimel-generate-id-from-heading "* Heading 1"))))

(ert-deftest test-gimel-add-custom-ids-to-headers-custom-headers ()
  "Test adding custom IDs to Org headers."
  (with-temp-buffer
    (org-mode)
    (insert "* Heading 1\n** Subheading A\n* Heading 2")
    (gimel-add-custom-ids-to-headers)

    (goto-char (point-min))
    (while (re-search-forward org-heading-regexp nil t)
      ;; Move to the beginning of the line to ensure we're at the heading
      (beginning-of-line)
      (let ((current-id (org-entry-get nil "CUSTOM_ID")))
        ;; Debug: Output the current point and ID
        (message "Point: %d, Current ID: %s" (point) current-id)
        ;; Assert that a custom ID is present
        (should current-id))
      ;; Move to the end of the current heading line
      (end-of-line))))

(ert-deftest test-gimel-add-custom-ids-to-headers-exisiting-headers ()
  "Test retention of existing custom IDs to Org headers."
  (with-temp-buffer
    (org-mode)
    ;; Insert sample headings, including one with an existing custom ID
    (insert "* Heading 1\n** Subheading A\n* Heading 2\n** Subheading B \n:PROPERTIES:\n:CUSTOM_ID: existing-id\n:END:")

    ;; Run the function to add custom IDs
    (gimel-add-custom-ids-to-headers)

    ;; Now check if the custom IDs were added and are correct
    (goto-char (point-min))
    (while (re-search-forward org-heading-regexp nil t)
      (beginning-of-line)
      (let* ((heading (org-get-heading t t))
             (current-id (org-entry-get nil "CUSTOM_ID"))
             (expected-id (if (string= heading "Subheading B")
                              "existing-id"
                            (gimel-generate-id-from-heading heading))))
        ;; Assert that a custom ID is present
        (should current-id)
        ;; Check if the custom ID matches the expected value
        (should (string= current-id expected-id))
        (end-of-line)))))

(ert-deftest test-gimel-preprocess-org-links-change ()
  "Test that `gimel-preprocess-org-links` transforms internal Org links correctly."
  (with-temp-buffer
    (org-mode)
    ;; Insert sample content with .org::*section] links
    (insert "This is a test buffer with [[example.org::*Example Heading][a link]].")

    ;; Run the function to preprocess links
    (gimel-preprocess-org-links)

    ;; Check if the content has been transformed correctly
    (goto-char (point-min))
    (should (re-search-forward "\\[\\[example\\.html#example-heading\\]\\[a link\\]\\]" nil t))))

(ert-deftest test-gimel-preprocess-org-links-no-change ()
  "Test that `gimel-preprocess-org-links` transforms internal Org links correctly."
  (with-temp-buffer
    (org-mode)
    ;; Insert sample content with .org::*section] links
    (insert "This is a test buffer with [[example.org][a link]]")

    ;; Run the function to preprocess links
    (gimel-preprocess-org-links)

    ;; Check if the content has been transformed correctly
    (goto-char (point-min))
    (should (re-search-forward "\\[\\[example\\.org\\]\\[a link\\]\\]" nil t))))

(ert-deftest test-gimel-postprocess-navbar-links ()
  "Test `gimel-postprocess-navbar-links` function."
  (should (string= (gimel-postprocess-navbar-links "Nam <a href=\"vestibulum/index.html\" accumsan nisl.")
                   "Nam <a href=\"/vestibulum/index.html\" accumsan nisl."))
  (should (string= (gimel-postprocess-navbar-links "Nam <a href=\"http://vestibulum/index.html\" accumsan nisl.")
                   "Nam <a href=\"http://vestibulum/index.html\" accumsan nisl.")))
