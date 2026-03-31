export default function JsonViewer({ title = "Response", data }) {
  return (
    <div className="card">
      <h3>{title}</h3>
      <pre className="json-panel">{JSON.stringify(data, null, 2)}</pre>
    </div>
  );
}
