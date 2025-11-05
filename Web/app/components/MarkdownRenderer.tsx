import type { ReactNode } from "react";

export type MarkdownRendererProps = {
  content: string;
  className?: string;
  labelledBy?: string;
};

type Block =
  | { type: "heading"; level: number; text: string }
  | { type: "paragraph"; text: string }
  | { type: "ul"; items: string[] }
  | { type: "ol"; items: string[] }
  | { type: "hr" };

function parseMarkdown(markdown: string): Block[] {
  const lines = markdown.replace(/\r\n?/g, "\n").split("\n");
  const blocks: Block[] = [];
  let currentParagraph: string[] = [];
  let currentList: { type: "ul" | "ol"; items: string[] } | null = null;

  const flushParagraph = () => {
    if (currentParagraph.length) {
      const text = currentParagraph.join(" ").trim();
      if (text) {
        blocks.push({ type: "paragraph", text });
      }
      currentParagraph = [];
    }
  };

  const flushList = () => {
    if (currentList && currentList.items.length) {
      blocks.push({ type: currentList.type, items: currentList.items });
    }
    currentList = null;
  };

  for (const line of lines) {
    const trimmed = line.trim();

    if (!trimmed) {
      flushParagraph();
      flushList();
      continue;
    }

    const headingMatch = trimmed.match(/^(#{1,6})\s+(.*)$/);
    if (headingMatch) {
      flushParagraph();
      flushList();
      const level = headingMatch[1].length;
      blocks.push({ type: "heading", level, text: headingMatch[2].trim() });
      continue;
    }

    const orderedMatch = trimmed.match(/^\d+\.\s+(.*)$/);
    if (orderedMatch) {
      flushParagraph();
      if (!currentList || currentList.type !== "ol") {
        flushList();
        currentList = { type: "ol", items: [] };
      }
      currentList.items.push(orderedMatch[1].trim());
      continue;
    }

    const unorderedMatch = trimmed.match(/^[-*+]\s+(.*)$/);
    if (unorderedMatch) {
      flushParagraph();
      if (!currentList || currentList.type !== "ul") {
        flushList();
        currentList = { type: "ul", items: [] };
      }
      currentList.items.push(unorderedMatch[1].trim());
      continue;
    }

    if (trimmed === "---" || trimmed === "***") {
      flushParagraph();
      flushList();
      blocks.push({ type: "hr" });
      continue;
    }

    currentParagraph.push(trimmed);
  }

  flushParagraph();
  flushList();

  return blocks;
}

function renderInline(text: string, keyPrefix: string): ReactNode[] {
  const elements: ReactNode[] = [];
  let remaining = text;
  let index = 0;

  const pattern = /(\*\*[^*]+\*\*|\*[^*]+\*|\[[^\]]+\]\([^\s)]+\))/;

  while (remaining.length) {
    const match = remaining.match(pattern);
    if (!match) {
      elements.push(remaining);
      break;
    }

    const [matched] = match;
    const matchIndex = match.index ?? 0;

    if (matchIndex > 0) {
      elements.push(remaining.slice(0, matchIndex));
    }

    const value = matched;
    if (value.startsWith("**")) {
      const content = value.slice(2, -2);
      elements.push(
        <strong key={`${keyPrefix}-strong-${index}`}>{content}</strong>
      );
    } else if (value.startsWith("*")) {
      const content = value.slice(1, -1);
      elements.push(<em key={`${keyPrefix}-em-${index}`}>{content}</em>);
    } else if (value.startsWith("[")) {
      const closingBracketIndex = value.indexOf("]");
      const label = value.slice(1, closingBracketIndex);
      const url = value.slice(closingBracketIndex + 2, -1);
      elements.push(
        <a key={`${keyPrefix}-link-${index}`} href={url} className="markdown-link">
          {label}
        </a>
      );
    }

    remaining = remaining.slice(matchIndex + value.length);
    index += 1;
  }

  return elements;
}

export function MarkdownRenderer({ content, className, labelledBy }: MarkdownRendererProps) {
  const blocks = parseMarkdown(content);

  return (
    <article className={className ?? "markdown"} aria-labelledby={labelledBy}>
      {blocks.map((block, blockIndex) => {
        const key = `${block.type}-${blockIndex}`;
        if (block.type === "heading") {
          const HeadingTag = `h${Math.min(block.level, 6)}` as keyof JSX.IntrinsicElements;
          return (
            <HeadingTag key={key} id={labelledBy && blockIndex === 0 ? labelledBy : undefined}>
              {renderInline(block.text, key)}
            </HeadingTag>
          );
        }

        if (block.type === "paragraph") {
          return (
            <p key={key}>
              {renderInline(block.text, key)}
            </p>
          );
        }

        if (block.type === "ul" || block.type === "ol") {
          const ListTag = block.type === "ul" ? "ul" : "ol";
          return (
            <ListTag key={key}>
              {block.items.map((item, itemIndex) => (
                <li key={`${key}-item-${itemIndex}`}>
                  {renderInline(item, `${key}-item-${itemIndex}`)}
                </li>
              ))}
            </ListTag>
          );
        }

        if (block.type === "hr") {
          return <hr key={key} />;
        }

        return null;
      })}
    </article>
  );
}
