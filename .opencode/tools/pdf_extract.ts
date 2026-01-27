import { tool } from "@opencode-ai/plugin"

export default tool({
  description: "Extract text, metadata, and images from a PDF",
  args: {
    path: tool.schema
      .string()
      .describe("Path to the PDF file to extract"),
  },
  async execute(args) {
    const result = await Bun.$`python3 .opencode/tools/pdf_extract.py ${args.path}`.text()
    return result.trim()
  },
})
