# 3D Render Engine in Java
This project implements a simple 3D render engine using Java Swing. It generates and displays 3D shapes and supports user interaction with sliders for horizontal and vertical rotation.

## 🎯 Idea Behind
The idea behind this 3D Render Engine is to create a simple graphical representation of 3D objects using Java. By implementing a basic rendering pipeline, this project showcases how to manipulate 3D shapes, apply transformations, and render them onto a 2D canvas. The engine can render colored triangles based on user-controlled rotations, making it a fun introduction to 3D graphics programming.

## ✨ Features
- Renders 3D triangles and objects.
- Interactive rotation via sliders for both heading and pitch.
- Efficient shading using z-buffering.
- Dynamic triangulation for smoother object surfaces.

## ⚙️ Working
The engine allows users to control the horizontal and vertical rotation of the rendered 3D object using sliders. Key features include:

- **User Interaction:** 
  - Two sliders control the horizontal (heading) and vertical (pitch) rotation angles of the 3D object.

- **Real-Time Rendering:**
  - The 3D triangles are rendered in real-time as the sliders are adjusted, providing immediate visual feedback.

- **Depth Buffering:**
  - Implements a simple z-buffer algorithm to manage depth, ensuring proper rendering of overlapping triangles.

- **Shading:**
  - Basic shading is applied based on the angle of the surface normals to create a more realistic appearance.

- **Inflation of Triangles:**
  - The triangles are recursively subdivided to create more detailed shapes, enhancing the visual complexity of the rendered object.

## 🔧 Technologies Used
- Java
- Swing
- AWT

## 📂 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/abhimittal1311/3D-Model-Render-Engine.git
   ```
2. Open the project in your preferred IDE (e.g., IntelliJ IDEA, Eclipse).
3. Run the `RenderEngine3D` class to start the application.

## 🚀 Usage

The application opens a window where you can adjust two sliders:
- **Horizontal Slider** (bottom) controls the heading (left/right rotation).
- **Vertical Slider** (right) controls the pitch (up/down rotation).

