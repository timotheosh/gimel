;;; gimel.el --- Description -*- lexical-binding: t; -*-
;;
;; Copyright (C) 2023 Tim Hawes
;;
;; Author: Tim Hawes <trhawes@gmail.com>
;; Maintainer: Tim Hawes <trhawes@gmail.com>
;; Created: December 11, 2023
;; Modified: December 11, 2023
;; Version: 0.0.1
;; Keywords: abbrev bib c calendar comm convenience data docs emulations extensions faces files frames games hardware help hypermedia i18n internal languages lisp local maint mail matching mouse multimedia news outlines processes terminals tex tools unix vc wp
;; Homepage: https://github.com/thawes/gimel
;; Package-Requires: ((emacs "25.1") (projectile "2.7.0"))
;;
;; This file is not part of GNU Emacs.
;;
;;; Commentary:
;;
;;  Description
;;
;;; Code:

(require 'url)

(defun gimel-path-append (&rest paths)
  "Join multiple PATHS into a single path with forward slashes."
  (let ((joined-path (mapconcat 'identity paths "/")))
    (replace-regexp-in-string "[\\/]+" "/" joined-path)))

(defcustom gimel-source-path "org"
  "The project subdirectory where we will find org-files."
  :type '(string)
  :group 'paths)

(defcustom gimel-target-path "html"
  "The project subdirectory where gimel will look for html snippets."
  :type '(string)
  :group 'paths)

(defcustom gimel-navbar-file "navbar.org"
  "The org-mode file used as a navigatio bar for the entire project's web site."
  :type '(string)
  :group 'paths)

(defcustom gimel-auto-publish nil
  "Whether or not to activate auto-publish on document save."
  :type '(string)
  :group 'paths)

(defcustom gimel-api-endpoint "http://localhost:8880"
  "The endpoint for gimel API."
  :type '(string)
  :group 'paths)

(defun gimel-export ()
  (url-retrieve (concat gimel-api-endpoint "/api/export")
                (lambda (status)
                  (if (plist-get status :error)
                      (message (format "Gimel returned error http status: %s" status))
                    (message "gimel site exported")))))

(defun gimel-split-org-content ()
  "Splits the org document on the first section. First part is treated as metadata.
   The second part is your document to be exported as html."
  (interactive)
  (save-excursion
    (goto-char (point-min))
    (if (re-search-forward "^\\*+ " nil t)
        (let ((first-part (buffer-substring-no-properties (point-min) (match-beginning 0)))
              (second-part (buffer-substring-no-properties (match-beginning 0) (point-max))))
          (list first-part second-part))
      (error "No headers found"))))

(defun gimel-handle-key-value-properties (key value)
  "Converts a key-value property into YAML format."
  (if (or (string-prefix-p "[" value)
          (string-prefix-p "\"" value)
          (string-prefix-p "{" value))
      (let ((json-value (json-read-from-string value)))
        (if (vectorp json-value)
            (concat (format "%s:" (downcase key))
                    (mapconcat (lambda (item) (format "\n  - %s" item))
                               json-value ""))
          (format "%s:\n  - %s" (downcase key) json-value)))
    (format "%s: \"%s\"" (downcase key) value)))

(defun gimel-org-metadata-to-yaml (org-content)
  "Converts the org-mode file-level metadata in a string into yaml format."
  (let ((lines (split-string org-content "\n"))
        (yaml-content '())
        (found-metadata nil))
    (dolist (line lines)
      (when (and (string-match "^#\\+\\(.*?\\): \\(.*\\)$" line)
                 (not (string-match "^#\\+\\(startup\\|options\\):" line)))  ; Exclude startup and options
        (unless found-metadata
          (push "---" yaml-content)
          (setq found-metadata t))
        (let ((key (match-string 1 line))
              (value (match-string 2 line)))
          (push (gimel-handle-key-value-properties key value) yaml-content))))
    (if found-metadata
        (progn  (push "---" yaml-content)
                (mapconcat 'identity (reverse yaml-content) "\n"))
      "")))


(defun gimel-get-source-path ()
  "Returns the source path."
  (gimel-path-append (projectile-project-root) gimel-source-path))

(defun gimel-get-target-path ()
  "Returns the target path."
  (gimel-path-append (projectile-project-root) gimel-target-path))

(defun gimel-target-path ()
  "Determines the target path for the exported document."
  (let ((source-dir (gimel-get-source-path))
        (target-dir (gimel-get-target-path)))
    (concat (file-name-sans-extension
             (gimel-path-append target-dir
                                (replace-regexp-in-string (regexp-quote source-dir) "" (buffer-file-name))))
            ".html")))

(defun gimel-copy-asset-files ()
  "Copy all asset files from the source directory to the target directory."
  (interactive)
  (let ((source-dir (gimel-get-source-path))
        (target-dir (gimel-get-target-path))
        (asset-extensions '("\\.jpg$" "\\.png$" "\\.gif$"
                            "\\.webp$" "\\.js$" "\\.css$"))  ; Add more extensions as needed
        files)
    ;; Find all asset files
    (dolist (ext asset-extensions)
      (setq files (append files (directory-files-recursively source-dir ext))))
    ;; Copy each asset file to the target directory
    (dolist (file files)
      (let ((target-file (gimel-path-append target-dir (file-relative-name file source-dir))))
        (make-directory (file-name-directory target-file) t)
        (copy-file file target-file t)))))

(defun gimel-generate-id-from-heading (heading)
  "Generate a URL-friendly custom ID based on the given HEADING."
  (downcase (replace-regexp-in-string "[^a-zA-Z0-9]+" "-" heading)))

(defun gimel-add-custom-ids-to-headers ()
  "Add custom IDs to each header in the current Org buffer."
  (org-map-entries (lambda ()
                     (let ((current-id (org-entry-get nil "CUSTOM_ID")))
                       (unless current-id
                         ;; Generate and set a custom ID for the header.
                         (let ((new-id (gimel-generate-id-from-heading (org-get-heading t t))))
                           (org-entry-put nil "CUSTOM_ID" new-id)))))))

(defun gimel-preprocess-org-links ()
  "Preprocess internal Org links to be direct HTML links."
  (goto-char (point-min))
  (while (re-search-forward "\\.org::\\*\\(.*?\\)\\]" nil t)
    (let ((section (match-string 1)))
      ;; Apply the same URL-friendly transformation as used in header IDs
      (let ((transformed-section (gimel-generate-id-from-heading section)))
        ;; Replace .org::*section] with .html#transformed-section]
        (replace-match (format ".html#%s]" transformed-section))))))

(defun gimel-preprocessors ()
  "Meta function that executes all the needed preprocessors for our generated org files before exporting them."
  (gimel-add-custom-ids-to-headers)
  (gimel-preprocess-org-links))

(defun gimel-postprocess-navbar-links (html-content)
  "Replace relative links in STR with absolute links."
  (replace-regexp-in-string "href=\"\\([^\"]*\\)\""
                            (lambda (match)
                              (let ((url (match-string 1 match)))
                                (if (or (string-prefix-p "http://" url)
                                        (string-prefix-p "https://" url)
                                        (string-prefix-p "javascript" url)
                                        (string-prefix-p "/" url))
                                    match
                                  (concat "href=\"/" url "\""))))
                            html-content))

(defun gimel-org-to-html-with-metadata ()
  "Converts an org-mode file to an HTML snippet and adds YAML metadata (from the file-level settings)  at the top."
  (interactive)
  (let* ((source-buffer buffer-file-name)
         (target-file (gimel-target-path))
         (split-content (gimel-split-org-content))
         (metadata (gimel-org-metadata-to-yaml (car split-content)))
         (org-content (cadr split-content))
         (html-content))
    ;; Ensure target directory exists.
    (make-directory (file-name-directory target-file) 'parents)
    (with-temp-buffer
      (insert org-content)
      (org-mode)
      (gimel-preprocessors)
      (let ((content (org-export-as 'html nil nil t '(:with-toc nil :section-numbers nil))))
        (if (and gimel-navbar-file
                 (string= (eshell/basename source-buffer) gimel-navbar-file))
            (setq html-content (gimel-postprocess-navbar-links content))
          (setq html-content content))))
    (message (format "Writing to %s" target-file))
    (with-temp-file target-file
      (insert metadata)
      (insert "\n")
      (insert html-content))
    nil))

(defun gimel-batch-process-org-files ()
  "Batch process all Org files from the source directory and save HTML output in the target directory."
  (interactive)
  (delete-directory (gimel-get-target-path) t)
  (gimel-copy-asset-files)
  (let ((source-dir (gimel-get-source-path))
        files)
    (setq files (directory-files-recursively source-dir "\\.org$"))
    (dolist (file files)
      (with-current-buffer (find-file-noselect file)
        ;; Process the file
        (gimel-org-to-html-with-metadata)
        (kill-buffer)))))


;; Example CLI command using Emacs batch mode:
;; emacs --batch -l gimel.el --eval '(gimel-batch-process-org-files "/path/to/source" "/path/to/target")'

(defun gimel-run-on-save ()
  "Run `gimel-org-to-html-with-metadata' if `gimel-auto-publish' is non-nil."
  (interactive)
  (when (and (eq major-mode 'org-mode) (boundp 'gimel-auto-publish) gimel-auto-publish)
    (progn
      (gimel-copy-asset-files)
      (gimel-org-to-html-with-metadata)
      (gimel-export))))

(provide 'gimel)
;;; gimel.el ends here
