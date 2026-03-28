export default function StatusBanner({ error, success, notice }) {
  if (error) {
    return <div className="error">{error}</div>;
  }
  if (success) {
    return <div className="success">{success}</div>;
  }
  if (notice) {
    return <div className="notice">{notice}</div>;
  }
  return null;
}
