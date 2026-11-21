# Navigation menu from page metadata

## Purpose

Pages declare where they appear in the site menu through metadata.
`gimel.metadata/generate-navmenu` turns those declarations into the menu HTML
that every exported page shows.

## Metadata keys

Written in Org as `#+menu: Races` and in Markdown/HTML frontmatter as
`menu: Races`.

| Key           | Meaning                                                                                  |
|---------------|------------------------------------------------------------------------------------------|
| `menu`        | The page is a top-level menu item. Its label is the value (`Races`).                     |
| `top-menu`    | The page is a top-level item in the reserved top group. Its label is the value (`Maps`). |
| `menu-parent` | The page is a sub-item under the menu item named by the value (`Characters`). Its label is the page's title. |

A menu item links to the page that declares it, using the page's exported URL
as an absolute path (leading `/`).

## Configuration

`menu-depth` in the `[server]` table of `gimel.toml` sets how many levels the
menu shows. It is optional and defaults to `2`. It must be a positive integer;
any other value is a configuration error that names the key. Existing config
files without it keep working.

| `menu-depth` | Levels shown                                         |
|--------------|------------------------------------------------------|
| `1`          | top-level items only                                 |
| `2`          | top-level items and their sub-items (the default)    |
| `3`          | one more level, and so on                            |

`generate-navmenu` receives the depth as an argument, so the menu is a function
of the page metadata and the depth alone.

## Behavior

1. **Ordering of top-level items.** Items from `top-menu` come first, then items
   from `menu`. Within each group, items are alphabetical by label, ignoring
   case.
2. **Ordering of sub-items.** The sub-items under any item are alphabetical by
   label (the page title), ignoring case.
3. **Any menu can be a parent.** A `menu-parent` value names a menu item by its
   label. The named item can be a top-level item or a sub-item, so menus nest as
   deep as the pages declare, limited by `menu-depth`.
4. **Missing parent.** When no menu item has the named label, a top-level item
   with that label is created in the `menu` group. It has no link, and the
   page appears under it.
5. **Same label, two items.** Two pages with the same label make two items,
   each linking to its own page, and a warning is logged naming the label and
   both URLs. Items with equal labels are ordered by page URL.
6. **Parent loops.** When parents form a loop (page A under B and B under A, or
   a page under its own label), the loop is broken by a duplicate root item.
   Among the pages in the loop, the one whose URL sorts first has its parent
   reference treated as unresolved. A copy of the item that reference names,
   with the same label and link, is created in the `menu` group at the top
   level, and the page goes under that copy. A warning is logged naming the
   pages in the loop.
7. **Depth limit.** Items deeper than `menu-depth` are not shown.
8. **No menu metadata.** A page without any of the three keys is not in the
   menu. A site where no page uses these keys gets the same navigation it gets
   today.
9. **Every page shows the whole menu.** All exported pages (Markdown and HTML)
   show the same menu, including pages that are processed before the page that
   declares an item.
10. **Export succeeds.** Exporting a site whose pages use `menu`, `top-menu` and
    `menu-parent` completes without error, whatever the parent names are,
    including duplicate labels and loops.
11. **`generate-navmenu` is a calculation.** It takes the metadata of the pages
    and the depth as data and returns the menu HTML together with the warnings
    it found, as data. It reads, writes and logs nothing; the export logs each
    warning.

## Output shape

Nested lists. An item with a page links to it; an item without a page is plain
text. Sub-items nest in a further `ul`.

```html
<ul>
  <li><a href="/maps.html">Maps</a></li>
  <li>
    <a href="/characters.html">Characters</a>
    <ul>
      <li><a href="/characters/aldric.html">Aldric</a></li>
      <li>
        <a href="/characters/mira.html">Mira</a>
        <ul>
          <li><a href="/characters/mira-journal.html">Journal</a></li>
        </ul>
      </li>
    </ul>
  </li>
  <li><a href="/races.html">Races</a></li>
</ul>
```

The third level appears only when `menu-depth` is `3` or more.

## Examples

Top-level ordering (`top-menu` first, then `menu`, each alphabetical):

| page            | key        | value   | position |
|-----------------|------------|---------|----------|
| `/atlas.html`   | `top-menu` | Atlas   | 1        |
| `/maps.html`    | `top-menu` | Maps    | 2        |
| `/classes.html` | `menu`     | Classes | 3        |
| `/races.html`   | `menu`     | Races   | 4        |

Parents:

| page                      | title   | `menu-parent` | appears                                 |
|---------------------------|---------|---------------|-----------------------------------------|
| `/characters/aldric.html` | Aldric  | Characters    | under Characters, before Mira           |
| `/characters/mira.html`   | Mira    | Characters    | under Characters                        |
| `/characters/journal.html`| Journal | Mira          | under Mira (a sub-item as parent)       |
| `/lore/origin.html`       | Origin  | Mythology     | under a new, unlinked top-level Mythology |

Same label (two pages with `menu: Guilds`, at `/a/guilds.html` and
`/b/guilds.html`): the menu has two Guilds items, `/a/guilds.html` first, each
linking to its own page, and one warning is logged.

Loop (`/alpha.html` titled Alpha with `menu-parent: Beta`, and `/beta.html`
titled Beta with `menu-parent: Alpha`): `/alpha.html` sorts first, so its
reference to Beta is unresolved. The menu has a top-level Beta, linking to
`/beta.html`, with Alpha under it and the original Beta under Alpha, and a
warning is logged naming both pages.

Depth (same pages, Journal is on the third level):

| `menu-depth` | Journal shown |
|--------------|---------------|
| `2`          | no            |
| `3`          | yes           |

## Assumptions to confirm

- Items deeper than `menu-depth` are dropped, not flattened onto the last
  level.
- A `menu-parent` value matches a label exactly (only ordering ignores case). If
  several items carry the label, the one whose page URL sorts first is the
  parent.
- A page with several keys appears once. Precedence: `top-menu`, then `menu`,
  then `menu-parent`.
- A loop is broken at the page whose URL sorts first, and the duplicate root is
  a copy of the item that page names as its parent (linked, not blank). The
  duplicate counts toward `menu-depth` like any other item.

## Out of scope

Taxonomies and categories, active-page highlighting, and changes to how the
Emacs exporter writes `#+menu` keys.
