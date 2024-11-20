using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace RestServer.EF
{
    [Table("task")]
    public class Task 
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("id")]
        public long Id { get; set; }

        [Column("task_name")]
        public string TaskName { get; set; }
        [Column("priority")]
        public string Priority { get; set; }
        [Column("status")]
        public string? Status { get; set; }
        [Column("notes")]
        public string? Notes { get; set; }
        [Column("archive_date")]
        public DateTime? ArchiveDate { get; set; }

    }
}
