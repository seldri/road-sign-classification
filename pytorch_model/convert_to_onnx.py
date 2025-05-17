import torch
from transformers import AutoModelForImageClassification
import os

# Load the model directly from Hugging Face
model = AutoModelForImageClassification.from_pretrained("bazyl/gtsrb-model")

# Set the model to evaluation mode
model.eval()

# Dummy input for the model (adjust size if needed)
dummy_input = torch.randn(1, 3, 224, 224)  # [batch, channels, height, width]

# Ensure the target directory exists
onnx_model_dir = "./src/main/resources/models/"
if not os.path.exists(onnx_model_dir):
    os.makedirs(onnx_model_dir, exist_ok=True)

onnx_model_path = os.path.join(onnx_model_dir, "gtsrb-model.onnx")

# Export to ONNX
torch.onnx.export(
    model, 
    dummy_input, 
    onnx_model_path,
    export_params=True,       # Store the trained weights
    opset_version=14,         # Updated to ONNX version 14
    input_names=['input'], 
    output_names=['output'],
    dynamic_axes={'input': {0: 'batch_size'}, 'output': {0: 'batch_size'}}
)

print(f"ONNX model has been saved to {onnx_model_path}")