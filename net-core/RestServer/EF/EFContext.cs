using Microsoft.EntityFrameworkCore;

namespace RestServer.EF
{
    public class EFContext(DbContextOptions<EFContext> options):
        DbContext(options)
    {
        public DbSet<Task> Tasks { get; set; }
    }
}
