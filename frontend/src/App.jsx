import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import AddWorkflow from "./components/workflows/AddWorkflow";
import UpdateWorkflow from "./components/workflows/UpdateWorkflow";
import DeleteWorkflow from "./components/workflows/DeleteWorkflow";
import ViewAllWorkflow from "./components/workflows/ViewAllWorkflows";
import ViewWorkflow from "./components/workflows/ViewWorkflow";
import ViewWorkflowByName from "./components/workflows/ViewWorkflowByName";
import ViewExecution from "./components/executions/ViewExecution";
import ViewAllExecutions from "./components/executions/ViewAllExecutions";
import RunWorkflow from "./components/executions/RunWorkflow";


export default function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/workflows/add" element={<AddWorkflow />} />
        <Route path="/workflows/update" element={<UpdateWorkflow />}/>
        <Route path="/workflows/delete" element={<DeleteWorkflow />}/>
        <Route path="/workflows/view" element={<ViewWorkflow />}/>
        <Route path="/workflows/view-all" element={<ViewAllWorkflow />}/>
        <Route path="/executions/view" element={<ViewExecution />}/>
        <Route path="/executions/view-all" element={<ViewAllExecutions />}/>
        <Route path="/executions/run" element={<RunWorkflow />}/>
        <Route path="/workflows/view/:name" element={<ViewWorkflowByName />} />
      </Routes>
    </Router>
  );
}
