import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { projectsAPI, modulesAPI, developersAPI } from '../api/api';

function ProjectDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [project, setProject] = useState(null);
  const [modules, setModules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModuleModal, setShowModuleModal] = useState(false);
  const [showDeveloperModal, setShowDeveloperModal] = useState(false);
  const [editingModule, setEditingModule] = useState(null);
  const [selectedModule, setSelectedModule] = useState(null);
  const [developers, setDevelopers] = useState([]);
  const [moduleDevelopers, setModuleDevelopers] = useState([]);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    technology: '',
    status: 'PENDING',
    priority: 'MEDIUM',
  });

  useEffect(() => {
    fetchData();
  }, [id]);

  const fetchData = async () => {
    try {
      const [projectRes, modulesRes] = await Promise.all([
        projectsAPI.getById(id),
        modulesAPI.getByProject(id),
      ]);
      setProject(projectRes.data.data);
      setModules(modulesRes.data.data);
    } catch (error) {
      console.error('Error fetching data:', error);
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const fetchModuleDevelopers = async (moduleId) => {
    try {
      const response = await modulesAPI.getDevelopers(moduleId);
      setModuleDevelopers(response.data.data);
    } catch (error) {
      console.error('Error fetching module developers:', error);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleModuleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingModule) {
        await modulesAPI.update(editingModule.id, formData);
      } else {
        await modulesAPI.create(id, formData);
      }
      setShowModuleModal(false);
      setEditingModule(null);
      resetForm();
      fetchData();
    } catch (error) {
      console.error('Error saving module:', error);
      alert(error.response?.data?.message || 'Error saving module');
    }
  };

  const handleEditModule = (module) => {
    setEditingModule(module);
    setFormData({
      name: module.name,
      description: module.description || '',
      technology: module.technology || '',
      status: module.status,
      priority: module.priority,
    });
    setShowModuleModal(true);
  };

  const handleDeleteModule = async (moduleId) => {
    if (window.confirm('Are you sure you want to delete this module?')) {
      try {
        await modulesAPI.delete(moduleId);
        fetchData();
      } catch (error) {
        console.error('Error deleting module:', error);
        alert(error.response?.data?.message || 'Error deleting module');
      }
    }
  };

  const handleOpenDeveloperModal = async (module) => {
    setSelectedModule(module);
    try {
      const [allDevs, mappedDevs] = await Promise.all([
        developersAPI.getAll(),
        modulesAPI.getDevelopers(module.id),
      ]);
      setDevelopers(allDevs.data.data);
      setModuleDevelopers(mappedDevs.data.data);
      setShowDeveloperModal(true);
    } catch (error) {
      console.error('Error fetching developers:', error);
    }
  };

  const handleMapDeveloper = async (developerId) => {
    try {
      await modulesAPI.mapDeveloper(selectedModule.id, developerId);
      fetchModuleDevelopers(selectedModule.id);
      fetchData();
    } catch (error) {
      console.error('Error mapping developer:', error);
    }
  };

  const handleUnmapDeveloper = async (developerId) => {
    try {
      await modulesAPI.unmapDeveloper(selectedModule.id, developerId);
      fetchModuleDevelopers(selectedModule.id);
      fetchData();
    } catch (error) {
      console.error('Error unmapping developer:', error);
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      technology: '',
      status: 'PENDING',
      priority: 'MEDIUM',
    });
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800';
      case 'ON_HOLD': return 'bg-yellow-100 text-yellow-800';
      case 'COMPLETED': return 'bg-blue-100 text-blue-800';
      case 'PENDING': return 'bg-gray-100 text-gray-800';
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'CRITICAL': return 'text-red-600';
      case 'HIGH': return 'text-orange-600';
      case 'MEDIUM': return 'text-yellow-600';
      case 'LOW': return 'text-green-600';
      default: return 'text-gray-600';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Project Info */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-2xl font-bold mb-2">{project.name}</h1>
              <p className="text-gray-600 mb-4">{project.description || 'No description'}</p>
              <div className="flex gap-4 text-sm text-gray-500">
                <span>Start: {project.startDate}</span>
                {project.endDate && <span>End: {project.endDate}</span>}
                {project.technologyStack && <span>Tech: {project.technologyStack}</span>}
              </div>
            </div>
            <div className="flex flex-col items-end gap-3">
              <span className={`px-3 py-1 rounded ${getStatusColor(project.status)}`}>
                {project.status}
              </span>
              <Link
                to={`/projects/${project.id}/bugs`}
                className="bg-red-600 hover:bg-red-700 text-white text-sm font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center gap-1.5"
              >
                <span>🐛</span> View Bugs
              </Link>
            </div>
          </div>
        </div>

        {/* Modules Section */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-semibold">Modules</h2>
            <button
              onClick={() => { resetForm(); setEditingModule(null); setShowModuleModal(true); }}
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
            >
              + New Module
            </button>
          </div>

          {modules.length === 0 ? (
            <div className="text-center py-8 text-gray-500">
              No modules yet. Create your first module!
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left py-3 px-4">Module Name</th>
                    <th className="text-left py-3 px-4">Technology</th>
                    <th className="text-left py-3 px-4">Status</th>
                    <th className="text-left py-3 px-4">Priority</th>
                    <th className="text-left py-3 px-4">Developers</th>
                    <th className="text-left py-3 px-4">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {modules.map((module) => (
                    <tr key={module.id} className="border-b hover:bg-gray-50">
                      <td className="py-3 px-4 font-medium">{module.name}</td>
                      <td className="py-3 px-4 text-gray-600">{module.technology || '-'}</td>
                      <td className="py-3 px-4">
                        <span className={`px-2 py-1 rounded text-xs ${getStatusColor(module.status)}`}>
                          {module.status}
                        </span>
                      </td>
                      <td className={`py-3 px-4 font-medium ${getPriorityColor(module.priority)}`}>
                        {module.priority}
                      </td>
                      <td className="py-3 px-4">
                        <button
                          onClick={() => handleOpenDeveloperModal(module)}
                          className="text-blue-600 hover:underline"
                        >
                          {module.developerCount || 0} assigned
                        </button>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleEditModule(module)}
                            className="text-gray-600 hover:text-gray-800"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDeleteModule(module.id)}
                            className="text-red-600 hover:text-red-800"
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </main>

      {/* Module Modal */}
      {showModuleModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-lg">
            <h3 className="text-xl font-bold mb-4">
              {editingModule ? 'Edit Module' : 'Create New Module'}
            </h3>
            <form onSubmit={handleModuleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Module Name *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Description</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                  rows="3"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1">Technology</label>
                <input
                  type="text"
                  name="technology"
                  value={formData.technology}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border rounded"
                />
              </div>
              <div className="grid grid-cols-2 gap-4 mb-6">
                <div>
                  <label className="block text-sm font-medium mb-1">Status *</label>
                  <select
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded"
                  >
                    <option value="PENDING">Pending</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Priority *</label>
                  <select
                    name="priority"
                    value={formData.priority}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded"
                  >
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="CRITICAL">Critical</option>
                  </select>
                </div>
              </div>
              <div className="flex gap-2 justify-end">
                <button
                  type="button"
                  onClick={() => { setShowModuleModal(false); setEditingModule(null); }}
                  className="px-4 py-2 border rounded hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                  {editingModule ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Developer Assignment Modal */}
      {showDeveloperModal && selectedModule && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[80vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4">
              Assign Developers to: {selectedModule.name}
            </h3>
            
            {/* Currently Assigned */}
            <div className="mb-6">
              <h4 className="font-medium mb-2">Currently Assigned ({moduleDevelopers.length})</h4>
              {moduleDevelopers.length === 0 ? (
                <p className="text-gray-500 text-sm">No developers assigned yet.</p>
              ) : (
                <div className="space-y-2">
                  {moduleDevelopers.map((dev) => (
                    <div key={dev.id} className="flex justify-between items-center bg-gray-50 p-2 rounded">
                      <div>
                        <span className="font-medium">{dev.name}</span>
                        <span className="text-gray-500 text-sm ml-2">{dev.email}</span>
                      </div>
                      <button
                        onClick={() => handleUnmapDeveloper(dev.id)}
                        className="text-red-600 hover:text-red-800 text-sm"
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Available Developers */}
            <div>
              <h4 className="font-medium mb-2">Available Developers</h4>
              {developers.length === 0 ? (
                <p className="text-gray-500 text-sm">No developers available. Add developers first.</p>
              ) : (
                <div className="space-y-2">
                  {developers
                    .filter((dev) => !moduleDevelopers.find((md) => md.id === dev.id))
                    .map((dev) => (
                      <div key={dev.id} className="flex justify-between items-center border p-2 rounded">
                        <div>
                          <span className="font-medium">{dev.name}</span>
                          <span className="text-gray-500 text-sm ml-2">{dev.email}</span>
                        </div>
                        <button
                          onClick={() => handleMapDeveloper(dev.id)}
                          className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700"
                        >
                          Assign
                        </button>
                      </div>
                    ))}
                </div>
              )}
            </div>

            <div className="mt-6 flex justify-end">
              <button
                onClick={() => { setShowDeveloperModal(false); setSelectedModule(null); }}
                className="px-4 py-2 border rounded hover:bg-gray-100"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ProjectDetail;