import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { bugsAPI, modulesAPI } from '../api/api';

function BugDetail() {
  const { id: bugId } = useParams();
  const navigate = useNavigate();
  const [bug, setBug] = useState(null);
  const [moduleDevelopers, setModuleDevelopers] = useState([]);
  const [selectedDevId, setSelectedDevId] = useState('');
  const [loading, setLoading] = useState(true);
  const [statusUpdating, setStatusUpdating] = useState(false);
  const [assigning, setAssigning] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Resolution modal state
  const [showResolveModal, setShowResolveModal] = useState(false);
  const [resolutionNotes, setResolutionNotes] = useState('');

  const [assignmentHistory, setAssignmentHistory] = useState([]);
  const [showHistory, setShowHistory] = useState(true);

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const isPM = user.role === 'PROJECT_MANAGER';
  const isTester = user.role === 'TESTER';
  const isDeveloper = user.role === 'DEVELOPER';

  useEffect(() => {
    fetchBug();
    fetchHistory();
  }, [bugId]);

  const fetchHistory = async () => {
    try {
      const res = await bugsAPI.getAssignmentHistory(bugId);
      setAssignmentHistory(res.data.data || []);
    } catch (e) {
      console.error('Error fetching assignment history:', e);
    }
  };

  const fetchBug = async () => {
    setLoading(true);
    try {
      const res = await bugsAPI.getById(bugId);
      const bugData = res.data.data;
      setBug(bugData);

      // If PM, fetch module-mapped developers for assignment dropdown
      if (isPM && bugData.moduleId) {
        try {
          const devsRes = await modulesAPI.getDevelopers(bugData.moduleId);
          setModuleDevelopers(devsRes.data.data || []);
        } catch (e) {
          console.error('Error fetching module developers:', e);
        }
      }
    } catch (err) {
      console.error('Error fetching bug:', err);
      setError('Bug not found or you do not have permission to view it.');
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (newStatus, notes = null) => {
    setStatusUpdating(true);
    setError('');
    setSuccess('');
    try {
      const res = await bugsAPI.changeStatus(bugId, newStatus, notes);
      setBug(res.data.data);
      setSuccess(`Status successfully updated to ${newStatus}`);
      setShowResolveModal(false);
    } catch (err) {
      console.error('Error changing status:', err);
      setError(err.response?.data?.message || 'Failed to update status.');
    } finally {
      setStatusUpdating(false);
    }
  };

  const handleAssign = async () => {
    if (!selectedDevId) return;
    setAssigning(true);
    setError('');
    setSuccess('');
    try {
      const res = await bugsAPI.assign(bugId, parseInt(selectedDevId, 10));
      setBug(res.data.data);
      setSelectedDevId('');
      setSuccess('Developer successfully assigned to this bug!');
      fetchHistory();
    } catch (err) {
      console.error('Error assigning developer:', err);
      setError(err.response?.data?.message || 'Failed to assign developer.');
    } finally {
      setAssigning(false);
    }
  };

  const handleUnassign = async () => {
    if (!window.confirm('Unassign this developer and return bug to OPEN?')) return;
    setAssigning(true);
    setError('');
    setSuccess('');
    try {
      const res = await bugsAPI.unassign(bugId);
      setBug(res.data.data);
      setSuccess('Bug successfully unassigned and returned to OPEN.');
      fetchHistory();
    } catch (err) {
      console.error('Error unassigning developer:', err);
      setError(err.response?.data?.message || 'Failed to unassign developer.');
    } finally {
      setAssigning(false);
    }
  };

  const handleDelete = async () => {
    if (window.confirm('Are you sure you want to permanently delete this bug?')) {
      try {
        await bugsAPI.delete(bugId);
        navigate(`/projects/${bug.projectId}/bugs`);
      } catch (err) {
        console.error('Error deleting bug:', err);
        setError(err.response?.data?.message || 'Error deleting bug.');
      }
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      OPEN: 'bg-yellow-100 text-yellow-800 border-yellow-300',
      ASSIGNED: 'bg-indigo-100 text-indigo-800 border-indigo-300 font-bold',
      IN_PROGRESS: 'bg-blue-100 text-blue-800 border-blue-300',
      RESOLVED: 'bg-purple-100 text-purple-800 border-purple-300',
      CLOSED: 'bg-green-100 text-green-800 border-green-300',
      REOPENED: 'bg-orange-100 text-orange-800 border-orange-300',
      REJECTED: 'bg-gray-100 text-gray-800 border-gray-300',
    };
    return (
      <span className={`px-3 py-1 rounded-full text-xs font-bold border ${styles[status] || 'bg-gray-100 text-gray-800'}`}>
        {status}
      </span>
    );
  };

  const getSlaBadge = (slaStatus) => {
    const styles = {
      ON_TRACK: 'bg-emerald-100 text-emerald-800 border-emerald-300',
      AT_RISK: 'bg-amber-100 text-amber-900 border-amber-300 font-bold',
      BREACHED: 'bg-rose-100 text-rose-800 border-rose-300 font-bold',
    };
    const labels = {
      ON_TRACK: 'SLA: On Track',
      AT_RISK: '⚠️ SLA: At Risk',
      BREACHED: '🚨 SLA: Breached',
    };
    return (
      <span className={`px-3 py-1 rounded-full text-xs font-bold border ${styles[slaStatus] || 'bg-gray-100 text-gray-800'}`}>
        {labels[slaStatus] || slaStatus}
      </span>
    );
  };

  const getSeverityBadge = (severity) => {
    const styles = {
      CRITICAL: 'bg-red-600 text-white',
      HIGH: 'bg-orange-500 text-white',
      MEDIUM: 'bg-yellow-500 text-white',
      LOW: 'bg-green-600 text-white',
    };
    return (
      <span className={`px-2.5 py-1 rounded text-xs font-bold ${styles[severity] || 'bg-gray-500 text-white'}`}>
        {severity}
      </span>
    );
  };

  const getPriorityBadge = (priority) => {
    const styles = {
      CRITICAL: 'bg-red-100 text-red-800 border border-red-200',
      HIGH: 'bg-orange-100 text-orange-800 border border-orange-200',
      MEDIUM: 'bg-yellow-100 text-yellow-800 border border-yellow-200',
      LOW: 'bg-green-100 text-green-800 border border-green-200',
    };
    return (
      <span className={`px-2.5 py-1 rounded text-xs font-semibold ${styles[priority] || 'bg-gray-100 text-gray-800'}`}>
        {priority} Priority
      </span>
    );
  };

  // Role-aware status actions
  const renderStatusActions = () => {
    if (!bug) return null;

    if (isDeveloper) {
      if (bug.status === 'ASSIGNED' || bug.status === 'REOPENED') {
        return (
          <button
            onClick={() => handleStatusChange('IN_PROGRESS')}
            disabled={statusUpdating}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-xs font-bold shadow-sm"
          >
            Start Work (In Progress)
          </button>
        );
      }
      if (bug.status === 'IN_PROGRESS') {
        return (
          <button
            onClick={() => {
              setResolutionNotes('');
              setShowResolveModal(true);
            }}
            disabled={statusUpdating}
            className="bg-purple-600 hover:bg-purple-700 text-white px-4 py-2 rounded-lg text-xs font-bold shadow-sm"
          >
            Submit Fix (Resolve)
          </button>
        );
      }
      return <span className="text-xs text-gray-400 italic">No workflow action required in this state</span>;
    }

    if (isTester) {
      if (bug.status === 'RESOLVED') {
        return (
          <div className="flex gap-2">
            <button
              onClick={() => handleStatusChange('CLOSED')}
              disabled={statusUpdating}
              className="bg-green-600 hover:bg-green-700 text-white px-3.5 py-1.5 rounded-lg text-xs font-bold shadow-sm"
            >
              Verify & Close
            </button>
            <button
              onClick={() => handleStatusChange('REOPENED')}
              disabled={statusUpdating}
              className="bg-orange-600 hover:bg-orange-700 text-white px-3.5 py-1.5 rounded-lg text-xs font-bold shadow-sm"
            >
              Fail Verification (Reopen)
            </button>
          </div>
        );
      }
      if (bug.status === 'CLOSED') {
        return (
          <button
            onClick={() => handleStatusChange('REOPENED')}
            disabled={statusUpdating}
            className="bg-orange-600 hover:bg-orange-700 text-white px-3.5 py-1.5 rounded-lg text-xs font-bold shadow-sm"
          >
            Reopen Bug
          </button>
        );
      }
      if (bug.status === 'OPEN') {
        return (
          <button
            onClick={() => handleStatusChange('REJECTED')}
            disabled={statusUpdating}
            className="bg-gray-600 hover:bg-gray-700 text-white px-3.5 py-1.5 rounded-lg text-xs font-bold shadow-sm"
          >
            Reject Invalid Bug
          </button>
        );
      }
      return null;
    }

    // PM actions
    if (isPM) {
      const pmActions = {
        OPEN: [{ label: 'Reject', target: 'REJECTED', style: 'bg-gray-600 hover:bg-gray-700 text-white' }],
        ASSIGNED: [{ label: 'Start Progress', target: 'IN_PROGRESS', style: 'bg-blue-600 hover:bg-blue-700 text-white' }],
        IN_PROGRESS: [{ label: 'Submit Fix', target: 'RESOLVED', action: () => setShowResolveModal(true), style: 'bg-purple-600 hover:bg-purple-700 text-white' }],
        RESOLVED: [
          { label: 'Close', target: 'CLOSED', style: 'bg-green-600 hover:bg-green-700 text-white' },
          { label: 'Reopen', target: 'REOPENED', style: 'bg-orange-600 hover:bg-orange-700 text-white' },
        ],
        CLOSED: [{ label: 'Reopen', target: 'REOPENED', style: 'bg-orange-600 hover:bg-orange-700 text-white' }],
        REOPENED: [{ label: 'Start Progress', target: 'IN_PROGRESS', style: 'bg-blue-600 hover:bg-blue-700 text-white' }],
        REJECTED: [{ label: 'Reopen', target: 'REOPENED', style: 'bg-orange-600 hover:bg-orange-700 text-white' }],
      };

      const acts = pmActions[bug.status] || [];
      return (
        <div className="flex flex-wrap items-center gap-2">
          {acts.map((act) => (
            <button
              key={act.target}
              onClick={act.action || (() => handleStatusChange(act.target))}
              disabled={statusUpdating}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold shadow-sm ${act.style}`}
            >
              {act.label}
            </button>
          ))}
        </div>
      );
    }

    return null;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-gray-500 font-medium">Loading bug details...</div>
      </div>
    );
  }

  if (error && !bug) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
        <div className="text-red-600 font-semibold mb-3">{error}</div>
        <Link to="/" className="text-blue-600 hover:underline text-sm font-medium">
          ← Return to Dashboard
        </Link>
      </div>
    );
  }

  const isBreached = bug.slaStatus === 'BREACHED';
  const isAtRisk = bug.slaStatus === 'AT_RISK';

  return (
    <div className="min-h-screen bg-gray-50 pb-16">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-5xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <Link
              to={isDeveloper ? '/my-bugs' : `/projects/${bug.projectId}/bugs`}
              className="text-blue-600 hover:text-blue-800 text-sm font-medium"
            >
              ← {isDeveloper ? 'Back to My Bugs' : `Back to ${bug.projectName} Bugs`}
            </Link>
          </div>
          <div className="flex items-center gap-2">
            {!isDeveloper && (
              <Link
                to={`/bugs/${bugId}/edit`}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3.5 py-1.5 rounded-lg text-xs font-semibold shadow-sm"
              >
                Edit Bug
              </Link>
            )}
            {isPM && (
              <button
                onClick={handleDelete}
                className="bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 px-3.5 py-1.5 rounded-lg text-xs font-semibold"
              >
                Delete Bug
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-5xl mx-auto px-4 py-6">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-6 text-sm">
            {success}
          </div>
        )}

        {/* SLA Breach Alert Banner */}
        {isBreached && (
          <div className="bg-rose-50 border-2 border-rose-400 text-rose-900 p-4 rounded-xl mb-6 shadow-sm flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="text-2xl">🚨</span>
              <div>
                <h3 className="font-bold text-base">SLA Deadline Breached!</h3>
                <p className="text-xs text-rose-700 mt-0.5">
                  This bug passed its resolution deadline on {new Date(bug.slaDeadline).toLocaleString()} and is currently <strong>{bug.slaRemainingTime}</strong>.
                </p>
              </div>
            </div>
            <span className="bg-rose-600 text-white px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
              Breached
            </span>
          </div>
        )}

        {/* SLA Warning Banner */}
        {isAtRisk && (
          <div className="bg-amber-50 border-2 border-amber-300 text-amber-900 p-4 rounded-xl mb-6 shadow-sm flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="text-2xl">⚠️</span>
              <div>
                <h3 className="font-bold text-base">SLA Deadline At Risk!</h3>
                <p className="text-xs text-amber-800 mt-0.5">
                  Remaining time before SLA breach: <strong>{bug.slaRemainingTime}</strong>.
                </p>
              </div>
            </div>
            <span className="bg-amber-500 text-white px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
              At Risk
            </span>
          </div>
        )}

        {/* Bug Header Card */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mb-6">
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div className="flex items-center gap-2 flex-wrap">
              <span className="font-mono text-sm font-bold text-gray-500">#{bug.id}</span>
              <span className="text-gray-300">•</span>
              <span className="text-sm font-semibold text-gray-700">Project: {bug.projectName}</span>
              <span className="text-gray-300">•</span>
              <span className="text-sm font-semibold text-gray-700">Module: {bug.moduleName}</span>
            </div>
            <div className="flex items-center gap-2 flex-wrap">
              {getSeverityBadge(bug.severity)}
              {getPriorityBadge(bug.priority)}
              {getSlaBadge(bug.slaStatus)}
              {getStatusBadge(bug.status)}
            </div>
          </div>

          <h1 className="text-2xl font-bold text-gray-900 mb-3">{bug.title}</h1>

          {/* Quick Lifecycle status bar */}
          <div className="bg-gray-50 p-4 rounded-lg border border-gray-100 mt-4 flex flex-wrap justify-between items-center gap-4">
            <div className="text-sm text-gray-600">
              Current Status: <strong className="text-gray-900 mr-3">{bug.status}</strong>
              SLA Countdown: <strong className={isBreached ? 'text-rose-600' : isAtRisk ? 'text-amber-700' : 'text-emerald-700'}>{bug.slaRemainingTime || '-'}</strong>
            </div>
            {renderStatusActions()}
          </div>
        </div>

        {/* 2-column Detail Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Info Column (2 cols) */}
          <div className="lg:col-span-2 space-y-6">
            {/* Resolution Notes Banner if present */}
            {bug.resolutionNotes && (
              <div className="bg-purple-50 rounded-xl border border-purple-200 p-5 shadow-sm">
                <h2 className="text-xs font-bold text-purple-900 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                  <span>💡</span> Developer Resolution Notes
                </h2>
                <p className="text-purple-950 text-sm whitespace-pre-wrap leading-relaxed font-medium">
                  {bug.resolutionNotes}
                </p>
              </div>
            )}

            {/* Description */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
              <h2 className="text-sm font-bold text-gray-700 uppercase tracking-wider mb-3">
                Description
              </h2>
              <p className="text-gray-800 text-sm whitespace-pre-wrap leading-relaxed">
                {bug.description || <span className="text-gray-400 italic">No description provided.</span>}
              </p>
            </div>

            {/* Steps to Reproduce */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
              <h2 className="text-sm font-bold text-gray-700 uppercase tracking-wider mb-3">
                Steps to Reproduce
              </h2>
              {bug.stepsToReproduce ? (
                <div className="bg-gray-50 p-4 rounded-lg font-mono text-xs text-gray-800 whitespace-pre-wrap leading-relaxed border border-gray-200">
                  {bug.stepsToReproduce}
                </div>
              ) : (
                <p className="text-gray-400 italic text-sm">No reproduction steps provided.</p>
              )}
            </div>

            {/* Expected vs Actual */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5">
                <h2 className="text-xs font-bold text-green-700 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                  <span>✓</span> Expected Result
                </h2>
                <p className="text-gray-800 text-sm whitespace-pre-wrap">
                  {bug.expectedResult || <span className="text-gray-400 italic text-xs">Not specified</span>}
                </p>
              </div>

              <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-5">
                <h2 className="text-xs font-bold text-red-700 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                  <span>✕</span> Actual Result
                </h2>
                <p className="text-gray-800 text-sm whitespace-pre-wrap">
                  {bug.actualResult || <span className="text-gray-400 italic text-xs">Not specified</span>}
                </p>
              </div>
            </div>
          </div>

          {/* Sidebar Metadata Column (1 col) */}
          <div className="space-y-6">
            {/* PM Developer Assignment Control */}
            {isPM && (
              <div className="bg-white rounded-xl border border-indigo-200 shadow-sm p-6 bg-indigo-50/10">
                <h2 className="text-sm font-bold text-indigo-900 uppercase tracking-wider mb-3 flex items-center gap-2">
                  <span>👤</span> Developer Assignment
                </h2>
                {bug.assignedDeveloperId ? (
                  <div className="space-y-3">
                    <div className="bg-indigo-50 border border-indigo-200 p-3 rounded-lg text-sm text-indigo-950">
                      <div className="text-xs text-indigo-700 font-semibold">Currently Assigned To:</div>
                      <div className="font-bold text-base mt-0.5">{bug.assignedDeveloperName}</div>
                    </div>
                    <button
                      onClick={handleUnassign}
                      disabled={assigning}
                      className="w-full bg-red-50 hover:bg-red-100 text-red-700 border border-red-200 px-3 py-2 rounded-lg text-xs font-bold transition-colors disabled:opacity-50"
                    >
                      {assigning ? 'Unassigning...' : 'Unassign Developer (Revert to OPEN)'}
                    </button>
                  </div>
                ) : (
                  <div className="space-y-3">
                    <p className="text-xs text-gray-500">
                      Assign to a developer mapped to module <strong>{bug.moduleName}</strong>:
                    </p>
                    {moduleDevelopers.length === 0 ? (
                      <div className="text-xs text-amber-800 bg-amber-50 p-2.5 rounded border border-amber-200">
                        No developers are mapped to this module yet. Please map developers in Module Management.
                      </div>
                    ) : (
                      <div className="space-y-2">
                        <select
                          value={selectedDevId}
                          onChange={(e) => setSelectedDevId(e.target.value)}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-indigo-500"
                        >
                          <option value="">Select Developer...</option>
                          {moduleDevelopers.map((dev) => (
                            <option key={dev.id} value={dev.id}>
                              {dev.name} ({dev.email})
                            </option>
                          ))}
                        </select>
                        <button
                          onClick={handleAssign}
                          disabled={!selectedDevId || assigning}
                          className="w-full bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-2 rounded-lg text-xs font-bold shadow-sm transition-colors disabled:opacity-50"
                        >
                          {assigning ? 'Assigning...' : 'Assign Developer'}
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </div>
            )}

            {/* SLA Policy Tracking Card */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
              <h2 className="text-sm font-bold text-gray-700 uppercase tracking-wider mb-4 flex items-center gap-2">
                <span>⏱️</span> SLA Tracking
              </h2>
              <dl className="space-y-3.5 text-sm">
                <div>
                  <dt className="text-xs text-gray-500 font-medium">SLA Status</dt>
                  <dd className="mt-1">{getSlaBadge(bug.slaStatus)}</dd>
                </div>

                <div>
                  <dt className="text-xs text-gray-500 font-medium">Resolution Deadline</dt>
                  <dd className="font-semibold text-gray-900 mt-0.5 text-xs">
                    {bug.slaDeadline ? new Date(bug.slaDeadline).toLocaleString() : 'Not Set'}
                  </dd>
                </div>

                <div>
                  <dt className="text-xs text-gray-500 font-medium">Countdown / Overdue</dt>
                  <dd className={`font-mono font-bold mt-0.5 text-sm ${isBreached ? 'text-rose-600' : isAtRisk ? 'text-amber-700' : 'text-emerald-700'}`}>
                    {bug.slaRemainingTime || '-'}
                  </dd>
                </div>
              </dl>
            </div>

            {/* Details & Metadata */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
              <h2 className="text-sm font-bold text-gray-700 uppercase tracking-wider mb-4">
                Details & Metadata
              </h2>
              <dl className="space-y-3.5 text-sm">
                <div>
                  <dt className="text-xs text-gray-500 font-medium">Reporter</dt>
                  <dd className="font-semibold text-gray-900 mt-0.5">{bug.reporterName || 'Unknown'}</dd>
                </div>

                <div>
                  <dt className="text-xs text-gray-500 font-medium">Assigned Developer</dt>
                  <dd className="font-semibold text-gray-900 mt-0.5">
                    {bug.assignedDeveloperName ? (
                      <span className="text-indigo-700 font-bold">{bug.assignedDeveloperName}</span>
                    ) : (
                      <span className="text-gray-400 italic font-normal">Unassigned</span>
                    )}
                  </dd>
                </div>

                <div>
                  <dt className="text-xs text-gray-500 font-medium">Environment</dt>
                  <dd className="font-medium text-gray-800 mt-0.5">
                    {bug.environment || <span className="text-gray-400 italic font-normal">None</span>}
                  </dd>
                </div>

                <div>
                  <dt className="text-xs text-gray-500 font-medium">Reported At</dt>
                  <dd className="text-xs text-gray-600 mt-0.5">
                    {bug.createdAt ? new Date(bug.createdAt).toLocaleString() : '-'}
                  </dd>
                </div>
              </dl>
            </div>
          </div>
        </div>

        {/* Assignment History Section */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mt-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-sm font-bold text-gray-700 uppercase tracking-wider flex items-center gap-2">
              <span>📜</span> Assignment History ({assignmentHistory.length})
            </h2>
            <button
              onClick={() => setShowHistory(!showHistory)}
              className="text-xs text-blue-600 hover:text-blue-800 font-semibold"
            >
              {showHistory ? 'Hide' : 'Show'}
            </button>
          </div>

          {showHistory && (
            assignmentHistory.length === 0 ? (
              <p className="text-xs text-gray-400 italic py-2">No developer assignments recorded yet.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-gray-50 border-b text-gray-500 uppercase font-semibold">
                    <tr>
                      <th className="py-2.5 px-3">Developer</th>
                      <th className="py-2.5 px-3">Assigned By</th>
                      <th className="py-2.5 px-3">Assigned At</th>
                      <th className="py-2.5 px-3">Unassigned At</th>
                      <th className="py-2.5 px-3">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {assignmentHistory.map((h) => (
                      <tr key={h.id} className="hover:bg-gray-50/80">
                        <td className="py-2.5 px-3 font-medium text-gray-900">
                          {h.developerName} <span className="text-gray-400 font-normal">({h.developerEmail})</span>
                        </td>
                        <td className="py-2.5 px-3 text-gray-600">{h.assignedByName || 'System'}</td>
                        <td className="py-2.5 px-3 text-gray-600">
                          {h.assignedAt ? new Date(h.assignedAt).toLocaleString() : '-'}
                        </td>
                        <td className="py-2.5 px-3 text-gray-600">
                          {h.unassignedAt ? new Date(h.unassignedAt).toLocaleString() : '-'}
                        </td>
                        <td className="py-2.5 px-3">
                          {h.isCurrent ? (
                            <span className="bg-green-100 text-green-800 px-2 py-0.5 rounded font-bold text-[11px]">
                              Active
                            </span>
                          ) : (
                            <span className="bg-gray-100 text-gray-600 px-2 py-0.5 rounded text-[11px]">
                              Released
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )
          )}
        </div>
      </main>

      {/* Resolve / Resolution Notes Modal */}
      {showResolveModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-lg w-full p-6 shadow-xl border border-gray-200 animate-in fade-in zoom-in duration-150">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold text-gray-900">
                Resolve Bug #{bug.id}
              </h3>
              <button
                onClick={() => setShowResolveModal(false)}
                className="text-gray-400 hover:text-gray-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>

            <form
              onSubmit={(e) => {
                e.preventDefault();
                handleStatusChange('RESOLVED', resolutionNotes);
              }}
              className="space-y-4"
            >
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Resolution Notes <span className="text-red-500">*</span>
                </label>
                <textarea
                  rows="4"
                  value={resolutionNotes}
                  onChange={(e) => setResolutionNotes(e.target.value)}
                  placeholder="Detail the cause of the bug and the technical fix applied..."
                  className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-purple-500"
                  required
                />
                <p className="text-xs text-gray-500 mt-1">
                  Resolution notes are required so testers know what to verify.
                </p>
              </div>

              <div className="flex justify-end gap-2 pt-4 border-t">
                <button
                  type="button"
                  onClick={() => setShowResolveModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-semibold text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={statusUpdating || !resolutionNotes.trim()}
                  className="px-5 py-2 bg-purple-600 text-white rounded-lg text-sm font-semibold hover:bg-purple-700 shadow-sm disabled:opacity-50"
                >
                  {statusUpdating ? 'Submitting...' : 'Mark Resolved'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default BugDetail;
