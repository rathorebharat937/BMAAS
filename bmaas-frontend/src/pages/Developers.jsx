import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { developersAPI } from '../api/api';

function Developers() {
  const navigate = useNavigate();
  const [developers, setDevelopers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingDeveloper, setEditingDeveloper] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    jobTitle: '',
    primarySkills: '',
    yearsOfExperience: '',
    department: '',
    employeeId: '',
    skillsTechStack: '',
    available: true,
  });

  useEffect(() => {
    fetchDevelopers();
  }, []);

  const fetchDevelopers = async () => {
    try {
      const response = await developersAPI.getAll();
      setDevelopers(response.data.data || []);
    } catch (error) {
      console.error('Error fetching developers:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        ...formData,
        yearsOfExperience: formData.yearsOfExperience ? parseFloat(formData.yearsOfExperience) : null,
      };
      if (editingDeveloper) {
        await developersAPI.update(editingDeveloper.id, payload);
      } else {
        await developersAPI.create(payload);
      }
      setShowModal(false);
      setEditingDeveloper(null);
      resetForm();
      fetchDevelopers();
    } catch (error) {
      console.error('Error saving developer:', error);
      alert(error.response?.data?.message || 'Error saving developer');
    }
  };

  const handleEdit = (developer) => {
    setEditingDeveloper(developer);
    setFormData({
      name: developer.name,
      email: developer.email,
      jobTitle: developer.jobTitle || '',
      primarySkills: developer.primarySkills || '',
      yearsOfExperience: developer.yearsOfExperience != null ? developer.yearsOfExperience.toString() : '',
      department: developer.department || '',
      employeeId: developer.employeeId || '',
      skillsTechStack: developer.skillsTechStack || '',
      available: developer.available !== undefined ? developer.available : true,
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this developer?')) {
      try {
        await developersAPI.delete(id);
        fetchDevelopers();
      } catch (error) {
        console.error('Error deleting developer:', error);
        alert(error.response?.data?.message || 'Error deleting developer');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      email: '',
      jobTitle: '',
      primarySkills: '',
      yearsOfExperience: '',
      department: '',
      employeeId: '',
      skillsTechStack: '',
      available: true,
    });
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl text-gray-600">Loading developers...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-blue-600 hover:underline">← Back to Dashboard</Link>
            <h1 className="text-2xl font-bold text-gray-900">Developer Management</h1>
          </div>
          <div className="flex items-center gap-4">
            <Link
              to="/leaderboard"
              className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-3.5 py-2 rounded shadow-sm"
            >
              📊 Performance Leaderboard
            </Link>
            <span className="text-gray-600 text-sm">Welcome, {user.fullName}</span>
            <button
              onClick={handleLogout}
              className="bg-gray-600 text-white px-3 py-1.5 rounded text-sm hover:bg-gray-700"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h2 className="text-xl font-semibold text-gray-900">All Developers ({developers.length})</h2>
            <p className="text-xs text-gray-500 mt-0.5">
              Enriched developer profiles, skills, availability status, module mappings, and historical metrics
            </p>
          </div>
          <button
            onClick={() => { resetForm(); setEditingDeveloper(null); setShowModal(true); }}
            className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition"
          >
            + Add Developer
          </button>
        </div>

        {/* Developers Table */}
        {developers.length === 0 ? (
          <div className="text-center py-12 text-gray-500 bg-white rounded-lg shadow">
            No developers yet. Add your first developer!
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow-md overflow-x-auto">
            <table className="w-full text-left">
              <thead className="bg-gray-50 text-xs text-gray-500 font-semibold uppercase">
                <tr>
                  <th className="py-3 px-4">Developer</th>
                  <th className="py-3 px-4">Role & Department</th>
                  <th className="py-3 px-4">Primary Skills & Exp</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">User Account</th>
                  <th className="py-3 px-4">Workload</th>
                  <th className="py-3 px-4">Resolved / Fix</th>
                  <th className="py-3 px-4">Modules</th>
                  <th className="py-3 px-4">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm">
                {developers.map((developer) => {
                  const m = developer.metrics;
                  return (
                    <tr key={developer.id} className="hover:bg-gray-50">
                      <td className="py-3 px-4">
                        <div className="font-semibold text-gray-900">{developer.name}</div>
                        <div className="text-xs text-gray-500">{developer.email}</div>
                        {developer.employeeId && (
                          <span className="text-[10px] font-mono text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded">
                            {developer.employeeId}
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-xs">
                        <div className="font-medium text-gray-800">{developer.jobTitle || 'Developer'}</div>
                        <div className="text-gray-500">{developer.department || '-'}</div>
                      </td>
                      <td className="py-3 px-4 text-xs max-w-xs">
                        {developer.yearsOfExperience != null && (
                          <span className="inline-block bg-blue-50 text-blue-700 px-2 py-0.5 rounded font-semibold text-[10px] mr-1.5 mb-1">
                            {developer.yearsOfExperience} yrs
                          </span>
                        )}
                        <span className="text-gray-600">
                          {developer.primarySkills || developer.skillsTechStack || '-'}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <span
                          className={`text-[10px] font-semibold px-2.5 py-1 rounded-full ${
                            developer.available
                              ? 'bg-green-100 text-green-800 border border-green-200'
                              : 'bg-amber-100 text-amber-800 border border-amber-200'
                          }`}
                        >
                          {developer.available ? 'Available' : 'On Leave'}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        {developer.userId ? (
                          <span className="bg-green-100 text-green-800 text-[11px] font-semibold px-2.5 py-1 rounded-full border border-green-200">
                            ✓ @{developer.userUsername}
                          </span>
                        ) : (
                          <span className="bg-gray-100 text-gray-500 text-[11px] px-2 py-0.5 rounded-full">
                            Not Linked
                          </span>
                        )}
                      </td>
                      <td className="py-3 px-4">
                        <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                          m && m.currentWorkload > 0
                            ? 'bg-blue-100 text-blue-800 border border-blue-200'
                            : 'bg-gray-100 text-gray-600'
                        }`}>
                          {m ? m.currentWorkload : 0} active
                        </span>
                      </td>
                      <td className="py-3 px-4 text-xs">
                        {m && m.bugsResolved > 0 ? (
                          <div>
                            <span className="font-bold text-gray-900">{m.bugsResolved} res</span>
                            <span className="text-gray-400 mx-1">•</span>
                            <span className="text-green-700 font-semibold">{m.firstTimeFixRate}%</span>
                          </div>
                        ) : (
                          <span className="text-gray-400 italic">No data</span>
                        )}
                      </td>
                      <td className="py-3 px-4">
                        <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded text-xs font-semibold">
                          {developer.moduleCount || 0}
                        </span>
                      </td>
                      <td className="py-3 px-4">
                        <div className="flex gap-2 text-xs">
                          <button
                            onClick={() => handleEdit(developer)}
                            className="text-blue-600 hover:text-blue-800 font-semibold"
                          >
                            Edit
                          </button>
                          <button
                            onClick={() => handleDelete(developer.id)}
                            className="text-red-600 hover:text-red-800 font-semibold"
                          >
                            Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </main>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-lg shadow-xl max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-gray-900">
              {editingDeveloper ? 'Edit Developer' : 'Add New Developer'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1 text-gray-700">Name *</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1 text-gray-700">Email *</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1 text-gray-700">Job Title / Designation</label>
                  <input
                    type="text"
                    name="jobTitle"
                    placeholder="e.g. Backend Developer"
                    value={formData.jobTitle}
                    onChange={handleChange}
                    className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1 text-gray-700">Department</label>
                  <input
                    type="text"
                    name="department"
                    placeholder="e.g. Engineering, QA"
                    value={formData.department}
                    onChange={handleChange}
                    className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium mb-1 text-gray-700">Employee ID</label>
                  <input
                    type="text"
                    name="employeeId"
                    placeholder="e.g. EMP-015"
                    value={formData.employeeId}
                    onChange={handleChange}
                    className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium mb-1 text-gray-700">Years of Experience</label>
                  <input
                    type="number"
                    step="0.1"
                    name="yearsOfExperience"
                    placeholder="e.g. 3.5"
                    value={formData.yearsOfExperience}
                    onChange={handleChange}
                    className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium mb-1 text-gray-700">Primary Skills</label>
                <input
                  type="text"
                  name="primarySkills"
                  value={formData.primarySkills}
                  onChange={handleChange}
                  placeholder="e.g., Java, Spring Boot, PostgreSQL, Docker"
                  className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium mb-1 text-gray-700">Full Tech Stack / Notes</label>
                <textarea
                  name="skillsTechStack"
                  value={formData.skillsTechStack}
                  onChange={handleChange}
                  className="w-full px-3 py-2 text-xs border rounded focus:ring-1 focus:ring-blue-500"
                  rows="2"
                  placeholder="Additional technologies or libraries..."
                />
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="available"
                  name="available"
                  checked={formData.available}
                  onChange={handleChange}
                  className="rounded text-blue-600"
                />
                <label htmlFor="available" className="text-xs font-medium text-gray-700 cursor-pointer">
                  Available for assignments (uncheck if on leave)
                </label>
              </div>

              <div className="flex gap-2 justify-end pt-3 border-t">
                <button
                  type="button"
                  onClick={() => { setShowModal(false); setEditingDeveloper(null); }}
                  className="px-4 py-2 border rounded text-xs text-gray-700 hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded text-xs font-semibold hover:bg-blue-700"
                >
                  {editingDeveloper ? 'Update Developer' : 'Add Developer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Developers;