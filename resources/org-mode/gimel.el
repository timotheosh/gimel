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

(defun gimel-split-org-content ()
  (interactive)
  (save-excursion
    (goto-char (point-min))
    (if (re-search-forward "^\\*+ " nil t)
        (let ((first-part (buffer-substring-no-properties (point-min) (match-beginning 0)))
              (second-part (buffer-substring-no-properties (match-beginning 0) (point-max))))
          (list first-part second-part))
      (error "No headers found"))))

(defun gimel-org-metadata-to-yaml (org-content)
  "Converts the org-mode file-level metadata in a string into yaml format."
  (let ((lines (split-string org-content "\n"))
        (yaml-content '()))
    (push "---" yaml-content)
    (dolist (line lines)
      (when (string-match "^#\\+\\(.*?\\): \\(.*\\)$" line)
        (let ((key (match-string 1 line))
              (value (match-string 2 line)))
          (push (format "%s: \"%s\"" (downcase key) value) yaml-content))))
    (push "---" yaml-content)
    (mapconcat 'identity (reverse yaml-content) "\n")))

(defun gimel-get-source-path ()
  "Returns the source path."
  (gimel-path-append (projectile-project-root) gimel-source-path))

(defun gimel-get-target-path ()
  "Returns the target path."
  (gimel-path-append (projectile-project-root) gimel-target-path))

(defun gimel-target-path ()
  (let ((source-dir (gimel-get-source-path))
        (target-dir (gimel-get-target-path)))
    (concat (file-name-sans-extension
             (gimel-path-append target-dir
                                (replace-regexp-in-string (regexp-quote source-dir) "" (buffer-file-name))))
            ".html")))

(defun gimel-copy-asset-files ()
  "Copy all asset files from the source directory to the target directory."
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
  ;; Convert heading to a URL-friendly format (e.g., lowercase, replace spaces and special chars)
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

(defun gimel-org-to-html-with-metadata ()
  "Converts an org-mode file to an HTML snippet and adds YAML metadata (from the file-level settings)  at the top."
  (interactive)
  (let* ((target-file (gimel-target-path))
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
      (setq html-content (org-export-as 'html nil nil t '(:with-toc nil))))
    (message (format "Writing to %s" target-file))
    (with-temp-file target-file
      (insert metadata)
      (insert "\n")
      (insert html-content))
    nil))

(defun gimel-batch-process-org-files ()
  "Batch process all Org files from the source directory and save HTML output in the target directory."
  (delete-directory (gimel-get-target-path) t)
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
  (when (and (boundp 'gimel-auto-publish) gimel-auto-publish)
    (progn
      (gimel-copy-asset-files)
      (gimel-org-to-html-with-metadata))))

(provide 'gimel)
;;; gimel.el ends here
