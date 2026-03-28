export default function ModuleHeader({ title, description, owner }) {
  return (
    <div className="page-header">
      <div className="module-pill">{owner}</div>
      <h2>{title}</h2>
      <p>{description}</p>
    </div>
  );
}
