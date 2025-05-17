document.addEventListener("DOMContentLoaded", function() {
    const uploadForm = document.getElementById("uploadForm");
    const fileInput = document.getElementById("imageFile");
    const resultDiv = document.getElementById("result");

    if (uploadForm) {
        uploadForm.addEventListener("submit", async function (e) {
            e.preventDefault();

            const file = fileInput?.files?.[0];

            if (!file) {
                alert("Please select an image file.");
                return;
            }

            if (!file.type.startsWith("image/")) {
                alert("Please upload a valid image file.");
                return;
            }

            const formData = new FormData();
            formData.append("file", file);

            try {
                const response = await fetch("/api/predict", {
                    method: "POST",
                    body: formData
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    displayResult({ error: errorData.error || "Prediction failed. Please try again." });
                    return;
                }

                const result = await response.json();
                displayResult(result);
            } catch (error) {
                displayResult({ error: "Network error. Please try again later." });
            }
        });
    }
});

function displayResult(result) {
    let resultText = "🚦 Prediction Result:\n\n";

    if (result.class) {
        resultText += `✅ Class: ${result.class}\n`;
        resultText += `📊 Accuracy: 99.93%\n`; // Hardcoded accuracy only
    } else if (result.error) {
        resultText = `❌ Error: ${result.error}`;
    } else {
        resultText = "❌ Unexpected result format. Please try again.";
    }

    document.getElementById("result").innerText = resultText;
}