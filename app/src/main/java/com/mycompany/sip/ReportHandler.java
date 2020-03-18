package com.mycompany.sip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.text.StaticLayout;
import android.text.TextPaint;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class handles the creation of a site report comprising of PDFs and CSVs.
 * This report is automatically saved to the device so that users may back it up or transfer it between devices.
 *
 * @Author Emily Fletcher
 * @Since 01/06/2020
 */
public class ReportHandler {
    PdfDocument document; //The document to be written to
    PdfDocument.PageInfo pageInfo; //info for a portrait-oriented page
    ArrayList<PdfDocument.Page> pages; //list of all of the pages which have not been written to the file
    Canvas canvas; //The canvas on which to draw
    Site site; //The site whose report is being created
    CSVWriter csvWriter; //writer to create CSV files
    File dir; //The directory to which the report should be saved
    String directory_path; //Path to the app's external directory
    int pageNum; //The index in the ArrayList of the current page

    //Page formatting guidelines
    final int pageWidth = 3000;
    final int pageHeight = 6000;
    final int horizontalMargin = 300;
    final int verticalMargin = 500;
    final int lineBreak = 100;
    final int sectionBreak = 200;
    final int headingTextSize = 200;
    final int plainTextSize = 100;

    //The vertical location of the first empty line on the page
    int bottomLineLoc = 0;

    //Various text formatting options
    TextPaint headingAlignCenter = new TextPaint();
    TextPaint headingAlignLeft = new TextPaint();
    TextPaint textAlignLeft = new TextPaint();


    /**
     * Constructor for the class
     * @param site the site whose report to generate
     * @param dir the application's external storage location
     */
    public ReportHandler(Site site, File dir) {
        //Initialize site
        this.site = site;

        //Create directory for the report
        directory_path = dir + "/MappPDFs/";
        this.dir = new File(directory_path);
        if (!this.dir.exists()) {
            this.dir.mkdirs();
        }

        //Initialize formatting paints
        headingAlignCenter.setTextAlign(Paint.Align.CENTER);
        headingAlignCenter.setTextSize(headingTextSize);
        headingAlignCenter.setColor(Color.BLACK);

        headingAlignLeft.setTextAlign(Paint.Align.LEFT);
        headingAlignLeft.setTextSize(headingTextSize);
        headingAlignLeft.setUnderlineText(true);
        headingAlignLeft.setColor(Color.BLACK);

        textAlignLeft.setTextAlign(Paint.Align.LEFT);
        textAlignLeft.setTextSize(plainTextSize);
        textAlignLeft.setColor(Color.BLACK);
    }

    /**
     * Generates the site report, which consists of information about the site and a list of its units.
     * Also creates a CSV list of all of the site's units.
     * @param site
     * @param allUnits
     */
    public void siteReport(Site site, ArrayList<Unit> allUnits) {
        //Create a CSV with the datum
        // Create file
        File temp = new File(directory_path + site.getNumber() + "_SiteDatum.csv");

        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(temp);

            // create CSVWriter object filewriter object as parameter
            csvWriter = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "Latitude", "Longitude", "Point Type"};
            csvWriter.writeNext(header);

            // add data to csv
            csvWriter.writeNext(new String[] {site.getDatum().latitude + "", site.getDatum().longitude + "", "Site Datum"});

            // closing writer connection
            csvWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }



        //Create a new document with portrait orientation
        document = new PdfDocument();
        pageNum = 0;
        pages = new ArrayList<PdfDocument.Page>();
        pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create();
        pages.add(document.startPage(pageInfo));
        canvas = pages.get(pageNum).getCanvas();

        bottomLineLoc = verticalMargin; //Start on the first line below the margin

        //Draw the site's name and number centered at the top of the page
        canvas.drawText(site.toString(), canvas.getWidth() / 2, bottomLineLoc, headingAlignCenter);
        bottomLineLoc += headingTextSize + lineBreak; //advance the bottom line

        //If the site has a datum, add it to the document
        if (!site.getDatum().equals("")) {
            canvas.drawText("Datum: ", horizontalMargin, bottomLineLoc, textAlignLeft);
            bottomLineLoc += sectionBreak;

            checkHeight(); //Make sure the page still has room
            canvas.drawText(site.getDatum().toString(), horizontalMargin * 2, bottomLineLoc, textAlignLeft); //Draw datum indented
            bottomLineLoc += sectionBreak;
        }

        //If the site has a date opened, add it to the document
        if (!site.getDateOpened().equals("")) {
            checkHeight(); //Make sure the page still has room
            canvas.drawText("Date discovered:", horizontalMargin, bottomLineLoc, textAlignLeft);
            bottomLineLoc += sectionBreak;

            checkHeight();
            canvas.drawText(site.getDateOpened().substring(0, Math.min(site.getDateOpened().length() - 1, 10)),
                    horizontalMargin * 2, bottomLineLoc, textAlignLeft); //Draw date opened indented, removing the timestamp portion
            bottomLineLoc += sectionBreak;
        }

        //If the site has a description, add it to the document
        if (!site.getDescription().equals("")) {
            checkHeight();
            canvas.drawText("Description:", horizontalMargin, bottomLineLoc, textAlignLeft);
            bottomLineLoc += sectionBreak;

            checkHeight();
            canvas.drawText(site.getDescription().substring(Math.min(site.getDescription().length()-1, 55)),
                    horizontalMargin * 2, bottomLineLoc, textAlignLeft); //Draw description indented, truncated if it is too long to fit on one line
            bottomLineLoc += headingTextSize + lineBreak;
        }

        //If the site has units, add them to the report
        if (!allUnits.isEmpty())
        {
            //Add "Units" heading to the document
            canvas.drawText("Units:", horizontalMargin, bottomLineLoc, headingAlignLeft);
            bottomLineLoc += sectionBreak;

            //Loop through all units to add them
            for( Unit u : allUnits)
            {
                //If the unit has a datum, add it
                if (!u.getDatum().equals("")) {
                    checkHeight();
                    canvas.drawText(u.getDatum() + ":", horizontalMargin, bottomLineLoc, textAlignLeft);
                    bottomLineLoc += sectionBreak;
                }

                //If the unit has dimensions, add them
                if (u.getEWDim()>-1 && u.getNSDim()>-1) {
                    checkHeight();
                    canvas.drawText("Dimensions: ", horizontalMargin * 2, bottomLineLoc, textAlignLeft); //Indented
                    bottomLineLoc += lineBreak;

                    checkHeight();
                    canvas.drawText(u.getNSDim() + "x" + u.getEWDim(),
                            horizontalMargin * 3, bottomLineLoc, textAlignLeft); //Indented below heading
                    bottomLineLoc += sectionBreak;
                }

                //If the unit has a reason for opening, add it
                if(!u.getReasonForOpening().equals("")) {
                    checkHeight();
                    canvas.drawText("Reason for opening:", horizontalMargin * 2, bottomLineLoc, textAlignLeft);
                    wrapText(u.getReasonForOpening(), horizontalMargin * 3); //Can be long, so be ready to wrap it across pages
                }

                //If the unit has a date opened, add it
                if(!u.getDateOpened().toString().equals("")) {
                    checkHeight();
                    canvas.drawText("Date opened: ", horizontalMargin * 2, bottomLineLoc, textAlignLeft);
                    bottomLineLoc += lineBreak;

                    checkHeight();
                    canvas.drawText(u.getDateOpened().substring(0, Math.min(u.getDateOpened().length() - 1, 10)),
                            horizontalMargin * 3, bottomLineLoc, textAlignLeft); //Add date opened indented, removing the timestamp
                    bottomLineLoc += sectionBreak + lineBreak;
                }
            }
            //Create a CSV list of the units
            addUnitsCSV(allUnits);
        }
        //Export the new document to storage
        export(directory_path + site.getNumber() + "_Report.pdf");
    }

    /**
     * Create a CSV list of the units
     * @param allUnits ArrayList of all units in the site
     */
    public void addUnitsCSV(ArrayList<Unit> allUnits) {

        // Create file
        File temp = new File(directory_path + site.getNumber() + "_Units.csv");

        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(temp);

            // create CSVWriter object filewriter object as parameter
            csvWriter = new CSVWriter(outputfile);

            // adding header to csv
            String[] header = { "Datum", "North/South Dimension", "East/West Dimension", "Date Opened", "Reason for Opening" };
            csvWriter.writeNext(header);

            // add data to csv
            for (Unit u : allUnits) {
                csvWriter.writeNext(u.tabulatedInfo());
            }

            // closing writer connection
            csvWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create reports for each level on the site, including a PDF document and CSV documents
     * @param allLevels The levels on the site
     * @param allArtifactBags The artifact bags on the site
     * @param allArtifacts The artifacts found at the site
     * @param allFeatures The features found at the site
     */
    public void addLevelReports(ArrayList<Level> allLevels, ArrayList<ArtifactBag> allArtifactBags, ArrayList<Artifact> allArtifacts, ArrayList<Feature> allFeatures){
        //If the site has levels
        if (!allLevels.isEmpty()) {
            //Create CSV files first
            // Create file
            File temp = new File(directory_path + site.getNumber() + "_Levels.csv");

            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(temp);

                // create CSVWriter object filewriter object as parameter
                csvWriter = new CSVWriter(outputfile);

                // adding header to csv
                String[] header = {"Unit", "Level Number", "Beginning Depth", "End Depth", "Date Started", "Excavation Method", "Level Notes"};
                csvWriter.writeNext(header);

                // add data to csv
                for (Level l : allLevels) {
                    csvWriter.writeNext(l.tabulatedInfo());
                }

                // closing writer connection
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Now, create PDF for each level
            for (Level l : allLevels) {
                //Each level gets its own document
                document = new PdfDocument();
                pageNum = 0;
                pages = new ArrayList<PdfDocument.Page>();
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create();
                pages.add(document.startPage(pageInfo));
                canvas = pages.get(pageNum).getCanvas();
                bottomLineLoc = verticalMargin; //Start at the first line below the margin

                //Draw headings: Site information, Unit information, and Level title
                canvas.drawText(site.toString(), canvas.getWidth() / 2, bottomLineLoc, headingAlignCenter);
                bottomLineLoc += headingTextSize + lineBreak;
                canvas.drawText(l.getUnit().toString(), canvas.getWidth() / 2, bottomLineLoc, headingAlignCenter);
                bottomLineLoc += headingTextSize + lineBreak;
                canvas.drawText(l.toString(), canvas.getWidth() / 2, bottomLineLoc, headingAlignCenter);
                bottomLineLoc += headingTextSize + lineBreak;

                //If the level has an image, add it to the document
                if (l.getImageUri() != "" && l.getImageUri() != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(Uri.parse(l.getImageUri()).getPath(), options);

                    int srcWidth = options.outWidth;
                    int srcHeight = options.outHeight;

                    int dstWidth = srcWidth;
                    int dstHeight = srcHeight;

                    float scale = (float) srcWidth / srcHeight;

                    //If the image is wider than it is tall, and its width is too big, resize it.
                    if (srcWidth > srcHeight && srcWidth > (pageWidth - (2 * horizontalMargin))) {
                        dstWidth = pageWidth - (2 * horizontalMargin);
                        dstHeight = Math.round(((pageWidth - (2 * horizontalMargin))) / scale);

                    } else if (srcHeight > srcWidth && srcHeight > (pageHeight - (2 * verticalMargin))) {
                        dstHeight = pageHeight - (2 * verticalMargin);
                        dstWidth = (int) ((pageHeight - (2 * verticalMargin)) / scale);
                    }
                    Paint canvasPaint = new Paint(Paint.DITHER_FLAG);
                    Bitmap bitmap = BitmapFactory.decodeFile(Uri.parse(l.getImageUri()).getPath());
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, false);

                    //Add the image to the document
                    canvas.drawBitmap(scaled, (pageWidth - dstWidth) / 2, bottomLineLoc, canvasPaint);
                    bottomLineLoc += sectionBreak + scaled.getHeight();
                }

                //Draw excavation techniques if they exist
                if(!l.getExcavationMethod().equals("")) {
                    checkHeight();
                    canvas.drawText("Excavation Techniques Used:", horizontalMargin, bottomLineLoc, headingAlignLeft);
                    bottomLineLoc += lineBreak;

                    //Will likely be long, so should be wrapped across multiple lines and even pages
                    wrapText(l.getExcavationMethod(), horizontalMargin);
                    bottomLineLoc += sectionBreak;
                }

                //Adds notes if they exist
                if(!l.getNotes().equals("")) {
                    checkHeight();
                    canvas.drawText("Level Notes:", horizontalMargin, bottomLineLoc, headingAlignLeft);
                    bottomLineLoc += lineBreak;

                    //Will likely be long, so should be wrapped across multiple lines and pages
                    wrapText(l.getNotes(), horizontalMargin);
                    bottomLineLoc += sectionBreak;
                }

                //Loops through all features to determine if any of them are linked to this level
                ArrayList<Feature> levelFeatures = new ArrayList<>();
                for (Feature f : allFeatures) {
                    if (f.getLevels().contains(l)) {
                        levelFeatures.add(f);
                    }
                }

                //If there are features associated with this level, add them to the document
                if (levelFeatures.size()>0) {
                    checkHeight();
                    canvas.drawText("Features:", horizontalMargin, bottomLineLoc, headingAlignLeft);
                    bottomLineLoc += headingTextSize;

                    //Loop through all level features to display
                    for (Feature f : levelFeatures) {
                        checkHeight();
                        canvas.drawText("Feature " + f.getNumber() + "\t" + f.getDescription().substring(0, Math.min(50, f.getDescription().length() - 1)),
                                horizontalMargin, bottomLineLoc, textAlignLeft); //Draws feature information with description, truncated if it is too long
                        bottomLineLoc += lineBreak;
                    }
                    bottomLineLoc += sectionBreak; //Once features are done, add a section break
                }

                //Loop through all artifact bags to determine if any of them are linked to this level
                ArrayList<ArtifactBag> levelABags = new ArrayList<>();
                for (ArtifactBag ab : allArtifactBags) {
                    if (ab.getLevel().equals(l)) {
                        levelABags.add(ab);
                    }
                }

                //If there are artifact bags associated with this level, add them to the document
                if (levelABags.size()>0) {
                    checkHeight();
                    canvas.drawText("Artifact Bags:", horizontalMargin, bottomLineLoc, headingAlignLeft);
                    bottomLineLoc += headingTextSize;

                    for (ArtifactBag ab : levelABags) {
                        checkHeight();
                        canvas.drawText(ab.toString().substring(0, Math.min(50, ab.toString().length() - 1)), horizontalMargin, bottomLineLoc, textAlignLeft);
                        bottomLineLoc += lineBreak;

                        //Loops through all artifacts to display the associated artifacts
                        for (Artifact a : allArtifacts) {
                            if (a.getArtifactBag().equals(ab)) {
                                checkHeight();
                                canvas.drawText(a.getDescription().substring(0, Math.min(50, a.getDescription().length() - 1)),
                                        horizontalMargin * 2, bottomLineLoc, textAlignLeft); //Draws description truncated in case it is long
                                bottomLineLoc += lineBreak;
                            }
                        }
                    }

                }
                //Export a document for each level
                export(directory_path + site.getNumber() + "-" + l.getUnit().toString() + "-" + l.getNumber() + ".pdf");
            }
        }
    }

    /**
     * Creates a CSV and PDF containing all of the site's artifacts
     * @param allArtifacts
     */
    public void addArtifactsCatalog(ArrayList<Artifact> allArtifacts) {
        if (!allArtifacts.isEmpty()) {
            // Create file
            File temp = new File(directory_path + site.getNumber() + "_ArtifactCatalog.csv");

            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(temp);

                // create CSVWriter object filewriter object as parameter
                csvWriter = new CSVWriter(outputfile);

                // adding header to csv
                String[] header = {"Unit", "Level", "Artifact Bag", "Description"};
                csvWriter.writeNext(header);

                // add data to csv
                for (Artifact a : allArtifacts) {
                    csvWriter.writeNext(a.tabulatedInfo());
                }

                // closing writer connection
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // start a pdf
            document = new PdfDocument();
            pageNum = 0;
            pages = new ArrayList<PdfDocument.Page>();
            pageInfo = new PdfDocument.PageInfo.Builder(pageHeight, pageWidth, pageNum + 1).create();
            pages.add(document.startPage(pageInfo));
            canvas = pages.get(pageNum).getCanvas();
            bottomLineLoc = verticalMargin;

            canvas.drawText("Artifact Catalog", horizontalMargin, bottomLineLoc, headingAlignLeft);
            bottomLineLoc += headingTextSize;

            //For each artifact, add it to the document
            for (Artifact a : allArtifacts) {
                checkHeight();
                canvas.drawText(a.tabulatedInfo()[0] + "\tLevel " + a.tabulatedInfo()[1] + "\tArtifact Bag " + a.tabulatedInfo()[2] + "\t"
                        + a.tabulatedInfo()[3].substring(Math.min(a.tabulatedInfo()[3].length()-1, 35)), //truncate text in case it is too long
                        horizontalMargin, bottomLineLoc, textAlignLeft);
                bottomLineLoc += plainTextSize + lineBreak;
            }

            export(directory_path + site.getNumber() + "_ArtifactCatalog.pdf");
        }
    }

    /**
     * Creates a CSV and PDF of all of the features associated with the site
     * @param allFeatures all of the features on the site
     */
    public void addFeaturesCatalog(ArrayList<Feature> allFeatures) {
        if (!allFeatures.isEmpty()) {
            // create file
            File temp = new File(directory_path + site.getNumber() + "_FeatureCatalog.csv");

            try {
                // create FileWriter object with file as parameter
                FileWriter outputfile = new FileWriter(temp);

                // create CSVWriter object filewriter object as parameter
                csvWriter = new CSVWriter(outputfile);

                // adding header to csv
                String[] header = {"Feature Number", "Description", "Units", "Levels"};
                csvWriter.writeNext(header);

                // add data to csv
                for (Feature f : allFeatures) {
                    csvWriter.writeNext(f.tabulatedInfo());
                    for (Level l : f.getLevels()) {
                        String[] level = {"", "", l.getUnit().toString(), l.getNumber() + ""};
                        csvWriter.writeNext(level);
                    }
                }

                // closing writer connection
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // start a pdf
            document = new PdfDocument();
            pageNum = 0;
            pages = new ArrayList<PdfDocument.Page>();
            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create();
            pages.add(document.startPage(pageInfo));
            canvas = pages.get(pageNum).getCanvas();
            bottomLineLoc = verticalMargin;

            //Draw a title
            canvas.drawText("Feature Catalog", horizontalMargin, bottomLineLoc, headingAlignLeft);
            bottomLineLoc += headingTextSize;

            //Loop through all features to add them to the page
            for (Feature f : allFeatures) {
                checkHeight();
                canvas.drawText("Feature " + f.tabulatedInfo()[0] + ":", horizontalMargin, bottomLineLoc, textAlignLeft);
                bottomLineLoc += sectionBreak;

                //If the feature has a description, display it
                if (!f.getDescription().equals("")) {
                    checkHeight();
                    canvas.drawText("Description:", horizontalMargin * 2, bottomLineLoc, textAlignLeft);
                    bottomLineLoc += lineBreak;

                    checkHeight();
                    canvas.drawText(f.getDescription().substring(Math.min(f.getDescription().length() - 1, 50)),
                            horizontalMargin * 3, bottomLineLoc, textAlignLeft);
                    bottomLineLoc += sectionBreak;
                }

                //If the feature is associated with levels, add them
                if (!f.getLevels().isEmpty()) {
                    checkHeight();
                    canvas.drawText("Levels:", horizontalMargin * 2, bottomLineLoc, textAlignLeft);
                    bottomLineLoc += lineBreak;

                    //Add each level
                    for (Level l : f.getLevels()) {
                        canvas.drawText(l.getUnit().toString() + " " + l.toString(), horizontalMargin * 3, bottomLineLoc, textAlignLeft);
                        bottomLineLoc += plainTextSize + lineBreak;
                        checkHeight();
                    }
                    bottomLineLoc += lineBreak;
                }
            }

            export(directory_path + site.getNumber() + "_FeatureCatalog.pdf");
        }
    }

    /**
     * Exports the current document to the given path
     * @param path
     */
    public void export(String path) {
        //If the document exists
        if(pageNum>-1) {
            //Finish the final page
            document.finishPage(pages.get(pageNum));

            File temp = new File(path);
            try {
                document.writeTo(new FileOutputStream(temp));
                document.close();
            } catch (IOException e) {
                System.err.println(e);
                // close the document
                document.close();
            }
        }
    }

    /**
     * Checks the height of the current page. If it is getting close to the bottom, creates a new page
     */
    private void checkHeight() {
        //If the page is nearing the end, create a new one
        if (bottomLineLoc >= .95*pageHeight) {
            document.finishPage(pages.get(pageNum));
            pageNum++;
            bottomLineLoc = verticalMargin;
            pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum+1).create();
            pages.add(document.startPage(pageInfo));
            canvas = pages.get(pageNum).getCanvas();
        }
    }

    /**
     * Wraps the text across lines and pages
     */
    private void wrapText(String text, int indentation) {
        int start = 0;
        int approxMaxChars = 0;
        String totalString = "";
        String substring = "";
        StaticLayout staticLayout = null;

        //While the text is not yet fully displayed on the page, continue to display it
        while (totalString.length() != text.length()) {
            //approxMaxChars--55 characters can fit on the page with this text size, the rest calculates the number of lines
                //It takes the minimum of that calculation and the text's length--if the text is short enough, it doesn't need to be wrapped across pages at all
            approxMaxChars = Math.min(55 * (pageHeight - bottomLineLoc) / (plainTextSize + lineBreak), text.length()-totalString.length());
            substring = text.substring(start, start + approxMaxChars);

            //We want to end the substring at a space if possible, unless it is the end of the text.
            //If it contains a space or a line break, lower approxMaxChars until it ends on that character
            while (totalString.length() + approxMaxChars != text.length() && text.charAt(start + approxMaxChars - 1) != ' ' && text.charAt(start + approxMaxChars - 1) != '\n' && (substring.contains(" ") || substring.contains("\n"))) {
                approxMaxChars--;
                substring = text.substring(start, start + approxMaxChars);
            }

            //Once the substring ends on white space, display it in a staticLayout so it wraps across lines and fills the page
            staticLayout = StaticLayout.Builder.obtain(substring, 0, substring.length(), textAlignLeft,
                    pageWidth - (2 * indentation)).build();
            canvas.save();
            canvas.translate(indentation, bottomLineLoc);
            staticLayout.draw(canvas);
            canvas.restore();
            totalString += substring;

            //If that wasn't the end of the text, create a new page
            if (totalString.length() != text.length()) {
                document.finishPage(pages.get(pageNum));
                pageNum++;
                bottomLineLoc = verticalMargin;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create();
                pages.add(document.startPage(pageInfo));
                canvas = pages.get(pageNum).getCanvas();
                start = approxMaxChars;
            } else {
                bottomLineLoc += staticLayout.getHeight() + (sectionBreak);
            }
        }
    }
}

