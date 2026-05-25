<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Print Timetable</title>
    <link rel="stylesheet" href="styles.css"> <!-- Assuming external CSS file -->
</head>
<body>

<div id="headerPrint">
    <div id="h1_div">
        <h1>Effortlessly Print Timetables in Multiple Formats!</h1>
    </div>
    <img src="resources/printImg.png" alt="printImg">
</div>

<div id="form-print">
 <!-- Right-side content -->
    <div class="right-side">
        <h2 style="text-align:center;">Select Options</h2>

        <!-- Container to hold the Format and Type Options horizontally -->
        <div class="options-container">
            <!-- Format Options -->
            <div class="option-group">
                <h3>Choose Format</h3> <br>
                <label>
                    <input type="checkbox" id="pdf_chk_box" name="format" value="PDF"> PDF
                </label>
                <label>
                    <input type="checkbox" id="excel_chk_box" name="format" value="Excel"> Excel
                </label>
            </div>

            <!-- Type Options -->
            <div class="option-group">
                <h3>Choose Type</h3><br>
                  <label>
                                    <input type="checkbox" id="semester_check_box" name="type" value="Department"> Department Wise
                                </label>
                                  <label>
                                                    <input type="checkbox" id="teacher_check_box" name="type" value="Teacher"> Teacher Wise
                                                </label>
                <label>
                    <input type="checkbox" id="room_check_box" name="type" value="Room/Lab"> Room / Lab Wise
                </label>


            </div>
        </div>

        <!-- Print Button (Centered) -->
        <div class="button-container">

            <button id="print_button">Print</button>
            <button id="download_button">Download</button>
        </div>
    </div>
    <!-- Left-side content -->
    <div class="left-side">
        <!-- Quick Tips Section -->
        <div id="quick-tips">
            <h3>Quick Tips</h3>
            <br>
            <ul>
                <li>Check <b>PDF</b> for a printer-friendly file.</li>
                <li>Use <b>Excel</b> for detailed editing.</li>
                <li>Choose the type of timetable before printing.</li>
            </ul>
        </div>

        <!-- Preview Section -->
        <div id="preview">
            <h3>Preview</h3>
            <div class="format-images">
                <div class="image-box">
                    <img src="resources/samplePrintpdf.png" alt="PDF Format">
                    <p>Sample PDF Output</p>
                </div>
                <div class="image-box">
                    <img src="resources/sampleprintexcel.png" alt="Excel Format">
                    <p>Sample Excel Output</p>
                </div>
            </div>
        </div>
    </div>


</div>

<div id="overlay-print"></div>
<div id="circle-print">
    <div class="stick-fast-print"></div>
    <div class="stick-slow-print"></div>
    <div id="origin-print"></div>
</div>


</body>
</html>
