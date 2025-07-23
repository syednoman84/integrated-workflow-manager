import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import AddWorkflow from "./components/workflows/AddWorkflow";
import UpdateWorkflow from "./components/workflows/UpdateWorkflow";
// import other components...

export default function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/workflows/add" element={<AddWorkflow />} />
        <Route path="/workflows/update" element={<UpdateWorkflow />}/>
        <Route path="/workflows/delete" element={<UpdateWorkflow />}/>
        <Route path="/workflows/view" element={<UpdateWorkflow />}/>
        <Route path="/workflows/view-all" element={<UpdateWorkflow />}/>
        <Route path="/executions/view" element={<UpdateWorkflow />}/>
        <Route path="/executions/view-all" element={<UpdateWorkflow />}/>
        <Route path="/executions/run" element={<UpdateWorkflow />}/>
      </Routes>
    </Router>
  );
}
