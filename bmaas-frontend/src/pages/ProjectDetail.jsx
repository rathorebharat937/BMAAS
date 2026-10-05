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
  const [editingModule, setEditingModule] = useState(null);
  const [expandedModuleId, setExpandedModuleId] = useState(null);

  // Developer data
  const [allDevelopers, setAllDevelopers] = useState([]);
  const [moduleMembersMap, setModuleMembersMap] = useState({});
  const [loadingMembers, setLoadingMembers] = useState({});
  const [roleInputs, setRoleInputs] = useState({});
  const [devSearchQuery, setDevSearchQuery] = useState('');
  const [onlyAvailableFilter, setOnlyAvailableFilter] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    technology: '',
    status: 'PENDING',
    priority: 'MEDIUM',
  });

  useEffect(() => {
    fetchData();
    fetchAllDevelopers();
  }, [id]);

  const fetchData = async () => {
    try {
      const [projectRes, modulesRes] = await Promise.all([
        projectsAPI.getById(id),
        modulesAPI.getByProject(id),
      ]);
      setProject(projectRes.data.data);
      const fetchedModules = modulesRes.data.data || [];
      setModules(fetchedModules);

      // Pre-fetch team members for each module so role badges show immediately
      fetchedModules.forEach((m) => {
        fetchMembersForModule(m.id);
      });
    } catch (error) {
      console.error('Error fetching data:', error);
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  const fetchAllDevelopers = async () => {
    try {
      const res = await developersAPI.getAll();
      setAllDevelopers(res.data.data || []);
    } catch (error) {
      console.error('Error fetching all developers:', error);
    }
  };

  const fetchMembersForModule = async (moduleId) => {
    setLoadingMembers((prev) => ({ ...prev, [moduleId]: true }));
    try {
      const res = await modulesAPI.getMembers(moduleId);
      setModuleMembersMap((prev) => ({
        ...prev,
        [moduleId]: res.data.data || [],
      }));
    } catch (error) {
      console.error(`Error fetching members for module ${moduleId}:`, error);
    } finally {
      setLoadingMembers((prev) => ({ ...prev, [moduleId]: false }));
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleModuleSubmit = async (e) => {
    e.preventDefault();
    try {
      let savedModuleId = null;
      if (editingModule) {
        await modulesAPI.update(editingModule.id, formData);
        savedModuleId = editingModule.id;
      } else {
        const createdRes = await modulesAPI.create(id, formData);
        savedModuleId = createdRes.data?.data?.id;
      }
      setShowModuleModal(false);
      setEditingModule(null);
      resetForm();
      await fetchData();

      // Automatically expand the saved module's Team section so PM can immediately assign developers
      if (savedModuleId) {
        setExpandedModuleId(savedModuleId);
        fetchMembersForModule(savedModuleId);
      }
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
        if (expandedModuleId === moduleId) {
          setExpandedModuleId(null);
        }
        fetchData();
      } catch (error) {
        console.error('Error deleting module:', error);
        alert(error.response?.data?.message || 'Error deleting module');
      }
    }
  };

  const toggleTeamSection = (moduleId) => {
    if (expandedModuleId === moduleId) {
      setExpandedModuleId(null);
    } else {
      setExpandedModuleId(moduleId);
      fetchMembersForModule(moduleId);
      fetchAllDevelopers();
    }
  };

  const handleAddDeveloperToModule = async (moduleId, developerId) => {
    const roleKey = `${moduleId}-${developerId}`;
    const roleOnModule = roleInputs[roleKey]?.trim() || '';
    try {
      await modulesAPI.mapDeveloper(moduleId, developerId, roleOnModule || null);
      // Clear input
      setRoleInputs((prev) => ({ ...prev, [roleKey]: '' }));
      await fetchMembersForModule(moduleId);
      await fetchData();
    } catch (error) {
      console.error('Error adding developer to module:', error);
      alert(error.response?.data?.message || 'Error assigning developer');
    }
  };

  const handleRemoveDeveloperFromModule = async (moduleId, developerId) => {
    if (window.confirm('Remove this developer from the module team?')) {
      try {
        await modulesAPI.unmapDeveloper(moduleId, developerId);
        await fetchMembersForModule(moduleId);
        await fetchData();
      } catch (error) {
        console.error('Error removing developer from module:', error);
        alert(error.response?.data?.message || 'Error removing developer');
      }
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
      case 'IN_PROGRESS': return 'bg-indigo-100 text-indigo-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getPriorityColor = (priority) => {
    switch (priority) {
      case 'CRITICAL': return 'text-red-600 bg-red-50';
      case 'HIGH': return 'text-orange-600 bg-orange-50';
      case 'MEDIUM': return 'text-yellow-600 bg-yellow-50';
      case 'LOW': return 'text-green-600 bg-green-50';
      default: return 'text-gray-600 bg-gray-50';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl text-gray-600">Loading project details...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-blue-600 hover:underline font-medium text-sm">
              ← Back to Dashboard
            </Link>
            <span className="text-gray-300">|</span>
            <Link to="/developers" className="text-gray-600 hover:text-blue-600 text-sm font-medium">
              👨‍💻 Standalone Developers Page
            </Link>
          </div>
          <div className="flex items-center gap-3">
            <Link
              to={`/projects/${project.id}/bugs`}
              className="bg-red-600 hover:bg-red-700 text-white text-sm font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center gap-1.5"
            >
              <span>🐛</span> Project Bugs
            </Link>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Project Info */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex justify-between items-start">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <h1 className="text-2xl font-bold text-gray-900">{project.name}</h1>
                <span className={`px-3 py-1 rounded text-xs font-semibold ${getStatusColor(project.status)}`}>
                  {project.status}
                </span>
              </div>
              <p className="text-gray-600 mb-4">{project.description || 'No description provided'}</p>
              <div className="flex flex-wrap gap-4 text-sm text-gray-500">
                <span>🗓️ <strong>Start:</strong> {project.startDate || 'N/A'}</span>
                {project.endDate && <span>🏁 <strong>End:</strong> {project.endDate}</span>}
                {project.technologyStack && (
                  <span>💻 <strong>Tech Stack:</strong> {project.technologyStack}</span>
                )}
                <span>📦 <strong>Modules:</strong> {modules.length}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Modules Section */}
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Project Modules</h2>
              <p className="text-xs text-gray-500 mt-0.5">
                Manage modules and assign developers directly with specific module roles
              </p>
            </div>
            <button
              onClick={() => { resetForm(); setEditingModule(null); setShowModuleModal(true); }}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 transition"
            >
              + New Module
            </button>
          </div>

          {modules.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              No modules yet. Click <strong>+ New Module</strong> to create your first module!
            </div>
          ) : (
            <div className="space-y-4">
              {modules.map((module) => {
                const isExpanded = expandedModuleId === module.id;
                const members = moduleMembersMap[module.id] || [];
                const membersCount = members.length;
                const assignedDevIds = new Set(members.map((m) => m.developerId));

                // Filter available developers for adding
                const filteredAvailableDevs = allDevelopers.filter((dev) => {
                  if (assignedDevIds.has(dev.id)) return false;
                  if (onlyAvailableFilter && !dev.available) return false;
                  if (!devSearchQuery.trim()) return true;
                  const q = devSearchQuery.toLowerCase();
                  return (
                    (dev.name && dev.name.toLowerCase().includes(q)) ||
                    (dev.jobTitle && dev.jobTitle.toLowerCase().includes(q)) ||
                    (dev.primarySkills && dev.primarySkills.toLowerCase().includes(q)) ||
                    (dev.department && dev.department.toLowerCase().includes(q)) ||
                    (dev.employeeId && dev.employeeId.toLowerCase().includes(q))
                  );
                });

                return (
                  <div
                    key={module.id}
                    className={`border rounded-xl transition ${
                      isExpanded ? 'border-blue-500 shadow-md ring-1 ring-blue-400' : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    {/* Module Row Header */}
                    <div className="p-4 flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gray-50 rounded-xl">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 flex-wrap">
                          <h3 className="text-lg font-bold text-gray-900">{module.name}</h3>
                          <span className={`px-2 py-0.5 rounded text-xs font-semibold ${getStatusColor(module.status)}`}>
                            {module.status}
                          </span>
                          <span className={`px-2 py-0.5 rounded text-xs font-semibold border ${getPriorityColor(module.priority)}`}>
                            {module.priority} Priority
                          </span>
                          {module.technology && (
                            <span className="text-xs bg-gray-200 text-gray-700 px-2 py-0.5 rounded font-mono">
                              {module.technology}
                            </span>
                          )}
                        </div>
                        {module.description && (
                          <p className="text-xs text-gray-600 mt-1 line-clamp-2">{module.description}</p>
                        )}

                        {/* Current Team preview badges on module view */}
                        <div className="mt-3 flex items-center gap-2 flex-wrap">
                          <span className="text-xs font-semibold text-gray-500">Team ({membersCount}):</span>
                          {membersCount === 0 ? (
                            <span className="text-xs text-gray-400 italic">No developers assigned yet</span>
                          ) : (
                            members.slice(0, 4).map((member) => (
                              <span
                                key={member.developerId}
                                className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-white border border-gray-300 text-gray-800 shadow-sm"
                              >
                                <span className="font-semibold">{member.developerName}</span>
                                {member.roleOnModule ? (
                                  <span className="bg-purple-100 text-purple-800 text-[10px] font-semibold px-1.5 py-0.5 rounded">
                                    {member.roleOnModule}
                                  </span>
                                ) : (
                                  <span className="bg-gray-100 text-gray-600 text-[10px] px-1.5 py-0.5 rounded">
                                    Contributor
                                  </span>
                                )}
                              </span>
                            ))
                          )}
                          {membersCount > 4 && (
                            <span className="text-xs text-blue-600 font-semibold">
                              +{membersCount - 4} more
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Actions */}
                      <div className="flex items-center gap-2 self-end md:self-center">
                        <button
                          onClick={() => toggleTeamSection(module.id)}
                          className={`px-3 py-1.5 rounded-lg text-xs font-semibold flex items-center gap-1.5 transition ${
                            isExpanded
                              ? 'bg-blue-600 text-white'
                              : 'bg-white border border-blue-600 text-blue-600 hover:bg-blue-50'
                          }`}
                        >
                          <span>👥</span>
                          <span>{isExpanded ? 'Hide Team Panel' : `Manage Team (${membersCount})`}</span>
                        </button>

                        <button
                          onClick={() => handleEditModule(module)}
                          className="px-2.5 py-1.5 rounded-lg text-xs font-medium text-gray-600 hover:bg-gray-200 transition"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDeleteModule(module.id)}
                          className="px-2.5 py-1.5 rounded-lg text-xs font-medium text-red-600 hover:bg-red-50 transition"
                        >
                          Delete
                        </button>
                      </div>
                    </div>

                    {/* EXPANDED TEAM SECTION (Part C) */}
                    {isExpanded && (
                      <div className="p-5 border-t border-gray-200 bg-white rounded-b-xl space-y-6">
                        {/* Section 1: Current Assigned Team */}
                        <div>
                          <div className="flex justify-between items-center mb-3">
                            <h4 className="text-sm font-bold text-gray-800 uppercase tracking-wider flex items-center gap-2">
                              <span>👥 Module Team Members</span>
                              <span className="bg-blue-100 text-blue-800 text-xs px-2 py-0.5 rounded-full font-bold">
                                {membersCount}
                              </span>
                            </h4>
                          </div>

                          {loadingMembers[module.id] ? (
                            <div className="text-xs text-gray-500 py-3">Loading team members...</div>
                          ) : membersCount === 0 ? (
                            <div className="p-4 bg-gray-50 rounded-lg text-xs text-gray-500 text-center border border-dashed border-gray-300">
                              No developers currently assigned to this module team. Add developers below.
                            </div>
                          ) : (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                              {members.map((member) => (
                                <div
                                  key={member.developerId}
                                  className="p-3 border rounded-lg bg-gray-50 flex flex-col justify-between hover:shadow-sm transition"
                                >
                                  <div>
                                    <div className="flex items-start justify-between gap-2">
                                      <div>
                                        <h5 className="font-bold text-gray-900 text-sm">
                                          {member.developerName}
                                        </h5>
                                        <div className="text-[11px] text-gray-500">
                                          {member.employeeId ? `[${member.employeeId}] ` : ''}
                                          {member.jobTitle || 'Developer'}
                                          {member.department ? ` • ${member.department}` : ''}
                                        </div>
                                      </div>
                                      <span
                                        className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                                          member.available
                                            ? 'bg-green-100 text-green-800'
                                            : 'bg-amber-100 text-amber-800'
                                        }`}
                                      >
                                        {member.available ? 'Available' : 'On Leave'}
                                      </span>
                                    </div>

                                    {/* Role on module badge */}
                                    <div className="mt-2 flex items-center gap-2">
                                      <span className="text-[11px] text-gray-500 font-medium">Module Role:</span>
                                      <span className="bg-purple-100 text-purple-800 text-xs font-semibold px-2 py-0.5 rounded border border-purple-200">
                                        {member.roleOnModule || 'Contributor'}
                                      </span>
                                    </div>

                                    {/* Skills & Experience */}
                                    <div className="mt-2 text-[11px] text-gray-600">
                                      {member.yearsOfExperience != null && (
                                        <span className="font-semibold text-gray-700">
                                          {member.yearsOfExperience} yrs exp •{' '}
                                        </span>
                                      )}
                                      <span className="text-gray-500">
                                        {member.primarySkills || 'No skills listed'}
                                      </span>
                                    </div>
                                  </div>

                                  <div className="mt-3 pt-2 border-t border-gray-200 flex justify-end">
                                    <button
                                      onClick={() => handleRemoveDeveloperFromModule(module.id, member.developerId)}
                                      className="text-red-600 hover:text-red-800 text-xs font-semibold flex items-center gap-1"
                                    >
                                      ✕ Remove from Team
                                    </button>
                                  </div>
                                </div>
                              ))}
                            </div>
                          )}
                        </div>

                        {/* Section 2: Add Developers to Module Team */}
                        <div className="border-t pt-5">
                          <h4 className="text-sm font-bold text-gray-800 uppercase tracking-wider mb-3">
                            ➕ Add Developer to {module.name} Team
                          </h4>

                          {/* Search and Filters */}
                          <div className="flex flex-col sm:flex-row gap-3 mb-4">
                            <div className="flex-1">
                              <input
                                type="text"
                                placeholder="🔍 Search developers by name, title, skills, or department..."
                                value={devSearchQuery}
                                onChange={(e) => setDevSearchQuery(e.target.value)}
                                className="w-full px-3 py-2 text-xs border rounded-lg focus:outline-none focus:ring-1 focus:ring-blue-500"
                              />
                            </div>
                            <label className="flex items-center gap-2 text-xs text-gray-700 cursor-pointer select-none">
                              <input
                                type="checkbox"
                                checked={onlyAvailableFilter}
                                onChange={(e) => setOnlyAvailableFilter(e.target.checked)}
                                className="rounded text-blue-600"
                              />
                              <span>Available Only</span>
                            </label>
                          </div>

                          {/* List of unassigned developers */}
                          {filteredAvailableDevs.length === 0 ? (
                            <div className="p-4 bg-gray-50 text-center text-xs text-gray-500 rounded-lg">
                              {allDevelopers.length === 0
                                ? 'No developers found in system. Seed or add developers first.'
                                : 'No unassigned developers matching the filter.'}
                            </div>
                          ) : (
                            <div className="space-y-2 max-h-72 overflow-y-auto pr-1">
                              {filteredAvailableDevs.map((dev) => {
                                const roleKey = `${module.id}-${dev.id}`;
                                const roleValue = roleInputs[roleKey] || '';

                                return (
                                  <div
                                    key={dev.id}
                                    className="p-3 border rounded-lg flex flex-col md:flex-row md:items-center justify-between gap-3 hover:bg-gray-50 text-xs"
                                  >
                                    <div className="flex-1">
                                      <div className="flex items-center gap-2 flex-wrap">
                                        <span className="font-bold text-gray-900 text-sm">{dev.name}</span>
                                        {dev.employeeId && (
                                          <span className="text-[11px] font-mono text-gray-500">
                                            ({dev.employeeId})
                                          </span>
                                        )}
                                        <span
                                          className={`text-[10px] font-semibold px-2 py-0.5 rounded-full ${
                                            dev.available
                                              ? 'bg-green-100 text-green-800'
                                              : 'bg-amber-100 text-amber-800'
                                          }`}
                                        >
                                          {dev.available ? 'Available' : 'On Leave'}
                                        </span>
                                        {dev.yearsOfExperience != null && (
                                          <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded font-semibold text-[10px]">
                                            {dev.yearsOfExperience} yrs exp
                                          </span>
                                        )}
                                      </div>

                                      <div className="text-gray-600 mt-1">
                                        <span className="font-medium text-gray-700">{dev.jobTitle || 'Developer'}</span>
                                        {dev.department && <span> • {dev.department}</span>}
                                        <span className="text-gray-400 mx-1">•</span>
                                        <span className="text-gray-500">{dev.email}</span>
                                      </div>

                                      {dev.primarySkills && (
                                        <div className="mt-1 text-gray-600">
                                          <span className="font-medium text-gray-700">Skills: </span>
                                          <span>{dev.primarySkills}</span>
                                        </div>
                                      )}
                                    </div>

                                    {/* Role Input and Add Button */}
                                    <div className="flex items-center gap-2">
                                      <input
                                        type="text"
                                        placeholder="Role (e.g. Lead, Contributor)"
                                        value={roleValue}
                                        onChange={(e) =>
                                          setRoleInputs((prev) => ({
                                            ...prev,
                                            [roleKey]: e.target.value,
                                          }))
                                        }
                                        className="px-2.5 py-1.5 border rounded text-xs w-48 focus:outline-none focus:ring-1 focus:ring-blue-500"
                                      />
                                      <button
                                        onClick={() => handleAddDeveloperToModule(module.id, dev.id)}
                                        className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1.5 rounded font-semibold text-xs transition whitespace-nowrap"
                                      >
                                        + Add to Module
                                      </button>
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </main>

      {/* Module Create/Edit Modal */}
      {showModuleModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-lg shadow-xl">
            <h3 className="text-xl font-bold mb-4 text-gray-900">
              {editingModule ? 'Edit Module' : 'Create New Module'}
            </h3>
            <form onSubmit={handleModuleSubmit}>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1 text-gray-700">Module Name *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="e.g., Authentication Service, Payment Gateway"
                  className="w-full px-3 py-2 border rounded focus:ring-1 focus:ring-blue-500"
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1 text-gray-700">Description</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder="Briefly describe what this module does..."
                  className="w-full px-3 py-2 border rounded focus:ring-1 focus:ring-blue-500"
                  rows="3"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-1 text-gray-700">Technology</label>
                <input
                  type="text"
                  name="technology"
                  value={formData.technology}
                  onChange={handleChange}
                  placeholder="e.g., Java, Spring Boot, React, PostgreSQL"
                  className="w-full px-3 py-2 border rounded focus:ring-1 focus:ring-blue-500"
                />
              </div>
              <div className="grid grid-cols-2 gap-4 mb-6">
                <div>
                  <label className="block text-sm font-medium mb-1 text-gray-700">Status *</label>
                  <select
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded focus:ring-1 focus:ring-blue-500"
                  >
                    <option value="PENDING">Pending</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1 text-gray-700">Priority *</label>
                  <select
                    name="priority"
                    value={formData.priority}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded focus:ring-1 focus:ring-blue-500"
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
                  className="px-4 py-2 border rounded text-gray-700 hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded font-medium hover:bg-blue-700"
                >
                  {editingModule ? 'Update Module' : 'Create & Assign Team'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default ProjectDetail;