# CTU report LaTeX template

This template recreates the visual system and document structure of the supplied Word report while keeping the source easy to reuse.

## Compile

Use XeLaTeX. From this directory, run it twice so the table of contents and lists are current:

```bash
xelatex main.tex
xelatex main.tex
```

The template uses Times New Roman when it is installed and falls back to TeX Gyre Termes. It requires a reasonably complete TeX Live or MiKTeX installation.

## Customize

Edit the metadata commands near the top of `main.tex`:

- `\CTUTitle`
- `\CTUStudent`
- `\CTUStudentID`
- `\CTUCohort`
- `\CTUSupervisor`
- `\CTUMonthYear`

Replace the sample prose and add or remove chapters as needed. Keep `ctu-report.sty` and the `assets` directory beside `main.tex`.

## Structural conventions

- Two cover pages, with a double border only on the outer cover.
- Front matter uses lowercase Roman page numbers.
- The abstract starts Arabic numbering at page 1.
- The introduction is unnumbered but has its own numbered subsections.
- Chapters use Roman numerals in the heading and Arabic numerals for sections, figures, and tables.
- Figure captions and table captions are centered and italic.
- Page numbers are centered in the footer; there are no running headers.

