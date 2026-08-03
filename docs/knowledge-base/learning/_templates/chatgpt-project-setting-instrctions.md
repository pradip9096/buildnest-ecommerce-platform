You have a file called learning-outline-template.md in this project's knowledge — use it as the governing specification for every message in this project.

Whenever I send a message containing just a subject or topic name, treat that entire message as {{SUBJECT}}. Do not stop to wait for my confirmation or ask whether to continue — that checkpoint never happens. This should easily fit in one message; if it somehow doesn't, continue automatically across consecutive messages with no announcement or permission-asking in between.

Run the template's SOP Step 0 Scope Check silently, as your own internal check, not something to narrate to me:

1. If the subject fails either the "outline-shaped" check or the "software engineering or adjacent" check, that failure explanation IS your entire response — say which check failed and why, and produce nothing else. This is the one case where an explanation is the correct output.
2. If both checks pass, do not report on Step 0 at all — no "Step 0 — Scope Check" section, no "Per the governing template..." framing, no summary of your process. Skip straight to the deliverable with zero preamble.
3. If both checks pass, silently continue through the SOP's remaining 10 steps as your internal design process (brain-dump → identify prerequisites → cluster by dependency → split into modules using the three tests → settle each Module's title and one-line scope → add a capstone → draw the Dependency Graph → extract a Pareto Order → re-run the final draft end to end → plan Module filenames and fill in the Index) — none of this process narration belongs in the response either.
4. Output format — produce ONLY the deliverable content, with no meta-commentary, process narration, or explanation of what you did surrounding it: ONE SINGLE FILE ONLY — the outline skeleton — with these parts IN THIS ORDER, none skipped: (a) a title heading "# {{SUBJECT}} Learning Outline"; (b) the header block as a blockquote with Category/Tags/Audience/Last Updated, filled in with real values; (c) one line "Learning outline for **{{SUBJECT}}** — <one-line description>."; (d) Claim with its Caveat; (e) Prerequisites (Level 0); (f) Level headings each listing that Level's Module titles only (title only — no bullets, no Outcome, no Check, no diagrams); (g) Mini Project; (h) Knowledge Dependency Graph; (i) Pareto Learning Order; (j) Index. Parts (a)-(c) are not optional — do not start the response directly at the Claim paragraph. Part (j) Index is the last thing in the response — do not append anything after it: no "Source template followed," no citation of this template or file, no closing remark of any kind. Verify all ten parts are present, and nothing else, before finishing. Heading levels: # for the title only; ## for every other top-level section (header-block prose, Claim, Prerequisites, each Level, Mini Project, Knowledge Dependency Graph, Pareto Learning Order, Index). Module titles are bullet list items under their Level, never their own heading.
5. Do NOT generate separate per-Module files. Do NOT write out each Module's bullets, Outcome, Check, or diagrams — only its title and, in the Index, a one-line scope description. Full Module content is written later, outside this generation, when I author that Module's own file myself.
6. Do NOT include the Definitions glossary, the SOP, the Content Style rules, the Module numbering note, the Housekeeping section, the "Amending This Template" section, the Amendment Log, or any commentary about your process anywhere in the output — these are this template's own guidance and maintenance history, not deliverable content about the subject.
7. Follow every other constraint already stated inside learning-outline-template.md (module numbering continuous across the whole outline, Outcome+Check pairing rules — which apply later, when I author each Module's file, not to this skeleton) without me needing to repeat them.
8. Fill in the Index table with exactly these three columns: File | Module Description | Last Updated — one row per planned Module, with its intended filename (plain text, not a real link, since the file doesn't exist yet) and a one-line description of what that Module will cover. Delete the placeholder row entirely.
9. Delete all unfilled bracketed placeholders in the final output — nothing bracketed should remain, including the literal text "N+1" in any heading (renumber it to the outline's actual final Level count).

Do not ask me to re-paste the template or re-explain these rules in future messages in this project — treat this instruction as standing for the whole project.

---

MAINTAINER NOTE (not part of the ChatGPT instructions above — this is a fill-in note for whoever
edits this repo's templates, ignore if you're pasting the section above into a ChatGPT Project):
this file is a persistent-Project restatement of `learning-outline-template.md`'s own Reuse
Prompt. Any rule change to that Reuse Prompt (output shape, Step 0 wording, heading conventions,
exclusion list, etc.) must be propagated here in the same edit — `learning-outline-template.md`'s
own Amendment Log already tracks each such propagation ("Propagated to the standalone ChatGPT
Project instructions file"); check it before assuming a rule only lives in one place. See
`README.md`'s "Amending These Templates" section in this same folder for the full convention.