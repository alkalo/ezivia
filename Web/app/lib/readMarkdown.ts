import { readFile } from "node:fs/promises";
import path from "node:path";

export async function readMarkdown(relativePath: string) {
  const absolutePath = path.join(process.cwd(), relativePath);
  const buffer = await readFile(absolutePath);
  return buffer.toString("utf-8");
}
