package framework.main;

public class FileType {

  TypeName type;
  private boolean isWeighted;
  private boolean isDirected;

  public FileType(String typeName) throws FileTypeException {
    isDirected = false;
    isWeighted = false;
    switch (typeName) {
      case "ID_ID_List":
      case "Directed_ID_ID_List":
        isDirected = true;
        isWeighted = false;
        type = TypeName.ID_ID_List;
        break;
      case "Undirected_ID_ID_List":
        isDirected = false;
        isWeighted = false;
        type = TypeName.ID_ID_List;
        break;
      case "Weighted_CSV":
      case "Directed_Weighted_CSV":
        isWeighted = true;
        isDirected = true;
        type = TypeName.Weighted_CSV;
        break;
      case "Unweighted_CSV":
      case "Directed_Unweighted_CSV":
        isDirected = true;
        isWeighted = false;
        type = TypeName.Unweighted_CSV;
        break;
      case "Undirected_Unweighted_CSV":
        type = TypeName.Unweighted_CSV;
        isDirected = false;
        isWeighted = false;
        break;
      case "Undirected_Weighted_CSV":
        isDirected = false;
        isWeighted = true;
        type = TypeName.Undirected_Weighted_CSV;
        break;
      default:
        throw new FileTypeException("File type invalid: " + typeName);
    }
  }


  public boolean isWeighted() {
    return isWeighted;
  }

  public boolean isDirected() {
    return isDirected;
  }

  public TypeName getType() {
    return type;
  }

  @Override
  public String toString() {
    return type + (isDirected ? "-Directed" : "-Undirected") + (isWeighted ? "-Weighted"
        : "-Unweighted");
  }

  public enum TypeName {
    ID_ID_List,
    Unweighted_CSV,
    Weighted_CSV,
    Undirected_Weighted_CSV
  }
}