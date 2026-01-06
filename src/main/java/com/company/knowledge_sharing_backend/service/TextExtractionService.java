package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.exception.InvalidFileTypeException;
import com.company.knowledge_sharing_backend.exception.TextExtractionException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class TextExtractionService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Extract text from uploaded file
     */
    public String extractText(MultipartFile file) {
        // Validate file
        validateFile(file);

        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename);

        try {
            return switch (extension.toLowerCase()) {
                case "pdf" -> extractFromPdf(file.getInputStream());
                case "doc" -> extractFromDoc(file.getInputStream());
                case "docx" -> extractFromDocx(file.getInputStream());
                case "xls" -> extractFromXls(file.getInputStream());
                case "xlsx" -> extractFromXlsx(file.getInputStream());
                case "ppt", "pptx" -> extractFromPptx(file.getInputStream());
                case "txt" -> extractFromTxt(file.getInputStream());
                default -> throw new InvalidFileTypeException("Unsupported file type: " + extension);
            };
        } catch (IOException e) {
            throw new TextExtractionException("Failed to extract text from file: " + filename, e);
        }
    }

    /**
     * Validate file type and size
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("No file provided");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileTypeException("File too large. Maximum size is 10MB");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new InvalidFileTypeException("Invalid file name");
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileTypeException(
                    "File type ." + extension + " is not supported. Allowed types: .doc, .docx, .pdf, .xls, .xlsx, .ppt, .pptx, .txt"
            );
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Extract text from PDF
     */
    private String extractFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Extract text from DOC (old Word format)
     */
    private String extractFromDoc(InputStream inputStream) throws IOException {
        try (HWPFDocument document = new HWPFDocument(inputStream);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * Extract text from DOCX
     */
    private String extractFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString();
        }
    }

    /**
     * Extract text from XLS (old Excel format)
     */
    private String extractFromXls(InputStream inputStream) throws IOException {
        try (HSSFWorkbook workbook = new HSSFWorkbook(inputStream)) {
            return extractFromWorkbook(workbook);
        }
    }

    /**
     * Extract text from XLSX
     */
    private String extractFromXlsx(InputStream inputStream) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            return extractFromWorkbook(workbook);
        }
    }

    /**
     * Extract text from Excel workbook (common for XLS and XLSX)
     */
    private String extractFromWorkbook(Workbook workbook) {
        StringBuilder text = new StringBuilder();
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    text.append(getCellValue(cell)).append(" ");
                }
                text.append("\n");
            }
        }
        return text.toString();
    }

    /**
     * Get cell value as string
     */
    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    /**
     * Extract text from PPTX
     */
    private String extractFromPptx(InputStream inputStream) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            StringBuilder text = new StringBuilder();
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        text.append(textShape.getText()).append("\n");
                    }
                }
            }
            return text.toString();
        }
    }

    /**
     * Extract text from TXT
     */
    private String extractFromTxt(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Truncate text if too long (to avoid GPT token limits)
     */
    public String truncateText(String text, int maxWords) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        if (words.length <= maxWords) {
            return text;
        }

        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            truncated.append(words[i]).append(" ");
        }
        return truncated.toString().trim();
    }
}

