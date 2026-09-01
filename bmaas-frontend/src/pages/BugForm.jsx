import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { projectsAPI, modulesAPI, bugsAPI } from '../api/api';

function BugForm() {
  const { projectId, id: bugId } = useParams();
  const navigate = useNavigate();
  const isEditMode = Boolean(bugId);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [project, setProject] = useState(null);
  const [modules, setModules] = useState([]);

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    moduleId: '',
    severity: 'MEDIUM',
    priority: 'MEDIUM',
    environment: '',
    stepsToReproduce: '',
    expectedResult: '',
    actualResult: '',
  });

  useEffect(() => {
    loadData();
  }, [projectId, bugId]);

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      if (isEditMode) {
        // Fetch existing bug
        const bugRes = await bugsAPI.getById(bugId);
        const bug = bugRes.data.data;
        setFormData({
          title: bug.title,
          description: bug.description || '',
          moduleId: bug.moduleId,
          severity: bug.severity,
          priority: bug.priority,
          environment: bug.environment || '',
          stepsToReproduce: bug.stepsToReproduce || '',
          expectedResult: bug.expectedResult || '',
          actualResult: bug.actualResult || '',
        });

        // Fetch project and modules for context
        const [projRes, modRes] = await Promise.all([
          projectsAPI.getById(bug.projectId),
          modulesAPI.getByProject(bug.projectId),
        ]);
        setProject(projRes.data.data);
        setModules(modRes.data.data);
      } else {
        // Create mode
        const [projRes, modRes] = await Promise.all([
          projectsAPI.getById(projectId),
          modulesAPI.getByProject(projectId),
        ]);
        setProject(projRes.data.data);
        setModules(modRes.data.data);
        if (modRes.data.data.length > 0) {
          setFormData((prev) => ({ ...prev, moduleId: modRes.data.data[0].id }));
        }
      }
    } catch (err) {
      console.error('Error loading bug form data:', err);
      setError('Failed to load form data. Please check if the project exists.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      setError('Bug title is required');
      return;
    }
    if (!isEditMode && !formData.moduleId) {
      setError('Please select a module');
      return;
    }

    setSaving(true);
    setError('');

    try {
      if (isEditMode) {
        await bugsAPI.update(bugId, formData);
        navigate(`/bugs/${bugId}`);
      } else {
        await bugsAPI.create(projectId, formData.moduleId, formData);
        navigate(`/projects/${projectId}/bugs`);
      }
    } catch (err) {
      console.error('Error saving bug:', err);
      setError(err.response?.data?.message || 'Failed to save bug.');
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500 font-medium">Loading form...</div>
      </div>
    );
  }

  const currentProjectId = isEditMode ? project?.id : projectId;

  return (
    <div className="min-h-screen bg-gray-50 pb-12">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-4xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <Link
              to={isEditMode ? `/bugs/${bugId}` : `/projects/${currentProjectId}/bugs`}
              className="text-blue-600 hover:text-blue-800 text-sm font-medium"
            >
              ← Back
            </Link>
            <span className="text-gray-300">/</span>
            <h1 className="text-xl font-bold text-gray-900">
              {isEditMode ? `Edit Bug #${bugId}` : `Report New Bug (${project?.name})`}
            </h1>
          </div>
        </div>
      </header>

      {/* Main Content Form */}
      <main className="max-w-4xl mx-auto px-4 py-6">
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 sm:p-8">
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Title */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                Bug Title <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                name="title"
                value={formData.title}
                onChange={handleChange}
                placeholder="e.g., Application crashes on submit button click in checkout"
                className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                required
              />
            </div>

            {/* Module, Severity, Priority in 3-col grid */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              {/* Module */}
              <div>
                <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                  Module {!isEditMode && <span className="text-red-500">*</span>}
                </label>
                <select
                  name="moduleId"
                  value={formData.moduleId}
                  onChange={handleChange}
                  disabled={isEditMode}
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:text-gray-500"
                  required
                >
                  {modules.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.name}
                    </option>
                  ))}
                </select>
                {isEditMode && (
                  <p className="text-xs text-gray-400 mt-1">Module cannot be changed after creation.</p>
                )}
              </div>

              {/* Severity */}
              <div>
                <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                  Severity <span className="text-red-500">*</span>
                </label>
                <select
                  name="severity"
                  value={formData.severity}
                  onChange={handleChange}
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500"
                >
                  <option value="CRITICAL">Critical (System Down / Blocker)</option>
                  <option value="HIGH">High (Major Feature Broken)</option>
                  <option value="MEDIUM">Medium (Normal Issue)</option>
                  <option value="LOW">Low (Minor / Cosmetic)</option>
                </select>
              </div>

              {/* Priority */}
              <div>
                <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                  Priority <span className="text-red-500">*</span>
                </label>
                <select
                  name="priority"
                  value={formData.priority}
                  onChange={handleChange}
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm bg-white focus:ring-2 focus:ring-blue-500"
                >
                  <option value="CRITICAL">Critical (Fix Immediately)</option>
                  <option value="HIGH">High (Fix in Current Sprint)</option>
                  <option value="MEDIUM">Medium (Fix in Next Release)</option>
                  <option value="LOW">Low (Fix When Convenient)</option>
                </select>
              </div>
            </div>

            {/* Environment */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                Environment / System Details
              </label>
              <input
                type="text"
                name="environment"
                value={formData.environment}
                onChange={handleChange}
                placeholder="e.g., macOS Sonoma, Chrome 128, Staging Server v1.4"
                className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                Description / Overview
              </label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows={3}
                placeholder="Provide a general summary of the issue..."
                className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Steps to Reproduce */}
            <div>
              <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                Steps to Reproduce
              </label>
              <textarea
                name="stepsToReproduce"
                value={formData.stepsToReproduce}
                onChange={handleChange}
                rows={4}
                placeholder="1. Navigate to /cart&#10;2. Click on 'Proceed to Checkout'&#10;3. Leave ZIP code empty and click Pay"
                className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm font-mono text-xs leading-relaxed focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Expected vs Actual Result (2 columns) */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                  Expected Result
                </label>
                <textarea
                  name="expectedResult"
                  value={formData.expectedResult}
                  onChange={handleChange}
                  rows={3}
                  placeholder="Form validation error should appear under ZIP field."
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-800 mb-1.5">
                  Actual Result
                </label>
                <textarea
                  name="actualResult"
                  value={formData.actualResult}
                  onChange={handleChange}
                  rows={3}
                  placeholder="White screen of death (500 Internal Server Error in console)."
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            {/* Action buttons */}
            <div className="flex justify-end items-center gap-3 pt-4 border-t">
              <button
                type="button"
                onClick={() =>
                  navigate(isEditMode ? `/bugs/${bugId}` : `/projects/${currentProjectId}/bugs`)
                }
                className="px-5 py-2.5 border border-gray-300 text-gray-700 rounded-lg text-sm font-semibold hover:bg-gray-100 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="px-6 py-2.5 bg-red-600 text-white rounded-lg text-sm font-semibold hover:bg-red-700 transition-colors shadow-sm disabled:opacity-50"
              >
                {saving ? 'Saving...' : isEditMode ? 'Update Bug' : 'Submit Bug Report'}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}

export default BugForm;
